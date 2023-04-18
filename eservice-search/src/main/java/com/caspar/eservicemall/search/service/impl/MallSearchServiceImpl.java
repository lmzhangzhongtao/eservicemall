package com.caspar.eservicemall.search.service.impl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.caspar.eservicemall.common.to.es.SkuEsModel;
import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.search.constant.EsConstant;
import com.caspar.eservicemall.search.feign.ProductFeignService;
import com.caspar.eservicemall.search.service.MallSearchService;
import com.caspar.eservicemall.search.util.ElasticsearchUtil;
import com.caspar.eservicemall.search.vo.AttrResponseVo;
import com.caspar.eservicemall.search.vo.BrandVo;
import com.caspar.eservicemall.search.vo.SearchParam;
import com.caspar.eservicemall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private ElasticsearchClient client;
    @Autowired
    ProductFeignService productFeignService;

    /**
     * 准备检索请求
     * #模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存），排序，分页，高亮，聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) throws IOException {
        List<Query> queryList = new ArrayList<>();
        /**
         * 模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）
         */
        //1.构建boolquery
        //1.1 must---模糊匹配
        List<Query> mustQuery = new ArrayList<>();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            Query matchQuery = MatchQuery.of(
                    m -> m.field("skuTitle").query(param.getKeyword())
            )._toQuery();
            mustQuery.add(matchQuery);
        }

        List<Query> filterQuery = new ArrayList<>();
        //1.2 bool -filter -按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            Query catalog3IdTermQuery = TermQuery.of(
                    t -> t.field("catalogId").value(param.getCatalog3Id())
            )._toQuery();
            filterQuery.add(catalog3IdTermQuery);
        }
        //1.2 bool -filter -按照品牌id进行查询
        List<FieldValue> brandIdFields = new ArrayList<>();
        if(param.getBrandId()!=null&&param.getBrandId().size()>0){
            for (Long brandId : param.getBrandId()) {
                brandIdFields.add(FieldValue.of(brandId));
                Query termsQuery = TermsQuery.of(t -> t.field("brandId").terms(terms -> terms.value(brandIdFields)))._toQuery();
                filterQuery.add(termsQuery);
            }
        }

        //1.2 bool -filter -按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            for (String attrStr : param.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_8G:16G
                String[] s = attrStr.split("_");
                String attrId = s[0];//检索的属性id
                String[] attrValues = s[1].split(":");//这个属性检索用的值

                List<Query> nestedboolqueryMust = new ArrayList<Query>();

                Query attrIdTermQuery = TermQuery.of(
                        t -> t.field("attrs.attrId").value(attrId)
                )._toQuery();
                List<FieldValue> attrValueField = new ArrayList<>();
                for (String s1 : attrValues) {
                    attrValueField.add(FieldValue.of(s1));
                }
                Query attrValusTermsQuery = TermsQuery.of(
                        t -> t.terms(terms -> terms.value(attrValueField)).field("attrs.attrValue")
                )._toQuery();
                nestedboolqueryMust.add(attrIdTermQuery);
                nestedboolqueryMust.add(attrValusTermsQuery);
                //每一个必须都生成一个nested查询
                Query nestedQuery = NestedQuery.of(
                        n -> n.path("attrs").query(
                                nestquerybuilder -> nestquerybuilder.bool(
                                        nestboolquerybuilder -> nestboolquerybuilder.must(nestedboolqueryMust)
                                )
                        )
                )._toQuery();
                filterQuery.add(nestedQuery);
            }

        }
        //1.2 bool -filter -按照是否有库存进行查询
        if (param.getHasStock() != null) {
            Query hasStockTermQuery = TermQuery.of(
                    t -> t.field("hasStock").value(param.getHasStock() == 1)
            )._toQuery();
            filterQuery.add(hasStockTermQuery);
        }
        //1.2 bool -filter -按照价格区间进行查询
        //1_500/_500/500_
        /**
         * "rang":{
         *     "skuPrice":{
         *         "gte":0,
         *         "lte::6000
         *     }
         * }
         */

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //区间
                Query rangeQuery = RangeQuery.of(
                        r -> r.field("skuPrice").gte(JsonData.of(s[0])).lte(JsonData.of(s[1]))
                )._toQuery();
                filterQuery.add(rangeQuery);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    Query rangeQuery = RangeQuery.of(
                            r -> r.field("skuPrice").lte(JsonData.of(s[1]))
                    )._toQuery();
                    filterQuery.add(rangeQuery);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    Query rangeQuery = RangeQuery.of(
                            r -> r.field("skuPrice").gte(JsonData.of(s[0]))
                    )._toQuery();
                    filterQuery.add(rangeQuery);
                }
            }
        }
        //构建bool查询，将所有条件都放进去
        BoolQuery boolQuery = BoolQuery.of(
                b -> b.must(mustQuery).filter(filterQuery)
        )._toQuery().bool();
        //把以前所有的条件都拿来进行封装
        Query query = new Query.Builder().bool(boolQuery)
                .build();
        /**
         * 排序，分页，高亮
         */
//        //2.1 排序
        List<SortOptions> sortOptionsList = new ArrayList<SortOptions>();
        if (!StringUtils.isEmpty(param.getSort())) {

            String sort = param.getSort();
            //sort=saleCount_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
            SortOptions sortOption = SortOptions.of(
                    sortOptionBuilder -> sortOptionBuilder.field(FieldSort.of(
                            f -> f.field(s[0]).order(order)
                    ))
            );
            sortOptionsList.add(sortOption);
        }
        //2.2分页
        //2.3高亮
        Highlight skuTitleHighLight = null;
        if (!StringUtils.isEmpty(param.getKeyword())) {
//            skuTitleHighLight = Highlight.of(
//                    highlightBuilder -> highlightBuilder
//                            .preTags("<b style='color:red'>")
//                            .postTags("</b>")
//                            .fields("skuTitle", highlightFieldBuilder -> highlightFieldBuilder)
//            );
             skuTitleHighLight=Highlight.of(
                    h->h.fields("skuTitle", HighlightField.of(
                            field->field.preTags("<span color='red'>").postTags("</span>")
                    ))
            );
        }
        /**
         * 聚合分析
         */
        //1.品牌聚合
        Map<String,Aggregation> aggregationMap=new HashMap<String,Aggregation>();
        Aggregation brandAgg = Aggregation.of(
                aggregationBuilder -> aggregationBuilder.terms(
                        t -> t.size(10).field("brandId")
                        ////品牌聚合的子聚合
                ).aggregations("brand_name_agg",brandNameAggBuilder->brandNameAggBuilder.terms(
                        brandNameTermsBuilder->brandNameTermsBuilder.field("brandName").size(1)
                )).aggregations("brand_img_agg",brandImgAggBuilder->brandImgAggBuilder.terms(
                        brandImgAggTermsBuilder->brandImgAggTermsBuilder.size(1).field("brandImg")
                ))
        );
        aggregationMap.put("brand_agg",brandAgg);

        //2.分类聚合
//        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
//        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
//        sourceBuilder.aggregation(catalog_agg);
        Aggregation catalog_agg=Aggregation.of(
                catalogAggBuilder->catalogAggBuilder.terms(
                        catalogAggTermsBuilder->catalogAggTermsBuilder.field("catalogId").size(20)
                ).aggregations("catalog_name_agg",catalogNameAggBuilder->catalogNameAggBuilder.terms(
                        catalogNameAggTermsBuilder->catalogNameAggTermsBuilder.field("catalogName").size(1)
                ))   // 子聚合
        );
        aggregationMap.put("catalog_agg",catalog_agg);
        //3.属性聚合
        Aggregation attr_agg=Aggregation.of(
                attrAggBuilder->attrAggBuilder.nested(
                        nestAttrAggBuilder->nestAttrAggBuilder.path("attrs")
                ).aggregations("attr_id_agg",attrIdAggBuilder->attrIdAggBuilder.terms(
                        attrAggTermsBuilder->attrAggTermsBuilder.field("attrs.attrId").size(10)
                        //attr_name_agg，attr_value_agg 是attr_id_agg的子聚合
                ).aggregations("attr_name_agg",attrNameAggBuilder->attrNameAggBuilder.terms(
                        attrNameAggTermsBuilder->attrNameAggTermsBuilder.field("attrs.attrName").size(10)
                )).aggregations("attr_value_agg",attrValueAggBuilder->attrValueAggBuilder.terms(
                        attrValueAggTermsBuilder->attrValueAggTermsBuilder.field("attrs.attrValue").size(10)
                )))
        );
        aggregationMap.put("attr_agg",attr_agg);

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(EsConstant.PRODUCT_INDEX)
                .query(query) // 传递条件
                .sort(sortOptionsList) //排序
                .from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE)
                .size(EsConstant.PRODUCT_PAGE_SIZE) //分页
                .highlight(skuTitleHighLight)
                .aggregations(aggregationMap) //聚合分析
                .build();
        String finalDSL=ElasticsearchUtil.printEsBySearchRequest(searchRequest);
        System.out.println("构建的DSL:"+finalDSL);
        return searchRequest;
    }


    @Override
    public SearchResult search(SearchParam param) throws IOException {
        SearchResult result = null;
//        //1.动态构建出查询需要的dsl语句
//        //1.准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
         try{
           //2.执行检索请求
             SearchResponse<Object> response = client.search(searchRequest, Object.class);
             //3.分析响应数据封装成我们需要的格式
             System.out.println("222222");
             result =buildSearchResult(response,param);


         }catch (Exception e){
             e.printStackTrace();
         }
        return result;
    }
    /**
     * 构建结果数据
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse<Object> response, SearchParam param) {

        SearchResult result = new SearchResult();

        HitsMetadata<Object> hits = response.hits();
        //1.返回的所有查询到的商品 result.setProducts()
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.hits()!=null && hits.hits().size()>0) {
            for (Hit<Object> hit:hits.hits()){
                SkuEsModel esModel=JSON.parseObject(JSON.toJSONString(hit.source()), SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    String str =hit.highlight().get("skuTitle").get(0);
                    esModel.setSkuTitle(str);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //获取聚合结果
        Map<String, Aggregate> aggregations = response.aggregations();

        //2.当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        NestedAggregate attrAgg = aggregations.get("attr_agg").nested();
        LongTermsAggregate  attr_id_gg= attrAgg.aggregations().get("attr_id_agg").lterms();
        Buckets<LongTermsBucket> buckets = attr_id_gg.buckets();
        for (LongTermsBucket bucket : attr_id_gg.buckets().array()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(bucket.key());
            StringTermsAggregate attrNameAgg = bucket.aggregations().get("attr_name_agg").sterms();
            attrVo.setAttrName((String)attrNameAgg.buckets().array().get(0).key()._get());
            //attrValues
           // List<String> attrValues=new ArrayList<>();
            StringTermsAggregate attrValueAgg = bucket.aggregations().get("attr_value_agg").sterms();
            List<String> attrValues = attrValueAgg.buckets().array().stream().map(
                    item -> (String)item.key()._get()
            ).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        //3.当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        LongTermsAggregate brand_agg =aggregations.get("brand_agg").lterms();
        for (LongTermsBucket bucket : brand_agg.buckets().array()){
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //获取品牌id
            brandVo.setBrandId(bucket.key());
            //获取品牌名称
            String brandName="";
            if(null!=bucket.aggregations().get("brand_name_agg")){
                if(bucket.aggregations().get("brand_name_agg").sterms().buckets()!=null&&bucket.aggregations().get("brand_name_agg").sterms().buckets().array().size()>0){
                    brandName=(String)bucket.aggregations().get("brand_name_agg").sterms().buckets().array().get(0).key()._get();
                }
            }
            //获取品牌照片
            String brandImg="";
            if(null!=bucket.aggregations().get("brand_img_agg")){
                if(bucket.aggregations().get("brand_img_agg").sterms().buckets()!=null&&bucket.aggregations().get("brand_img_agg").sterms().buckets().array().size()>0){
                    brandImg=(String) bucket.aggregations().get("brand_img_agg").sterms().buckets().array().get(0).key()._get();
                }
            }
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        //4.当前所有商品涉及到的所有分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        LongTermsAggregate catalog_agg =aggregations.get("catalog_agg").lterms();

        for (LongTermsBucket bucket : catalog_agg.buckets().array()){
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            catalogVo.setCatalogId(bucket.key());
            //得到分类名
            String catalogName="";
            if(null!=bucket.aggregations().get("catalog_name_agg")){
                if(bucket.aggregations().get("catalog_name_agg").sterms().buckets()!=null&&bucket.aggregations().get("catalog_name_agg").sterms().buckets().array().size()>0){
                    catalogName=(String) bucket.aggregations().get("catalog_name_agg").sterms().buckets().array().get(0).key()._get();
                }
            }
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //===================以上从聚合信息中获取======================
       // 5.分页信息--页码
        result.setPageNum(param.getPageNum());
        //5.分页信息--总记录数
        long total = hits.total().value();
        result.setTotal(total);
       //5. 分页信息--总页码
        int totalPages = (int)total%EsConstant.PRODUCT_PAGE_SIZE == 0?(int)total/EsConstant.PRODUCT_PAGE_SIZE:(int)total/EsConstant.PRODUCT_PAGE_SIZE+1;
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //6.构建面包屑导航功能
        if (param.getAttrs()!=null&&param.getAttrs().size()>0) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //1.分析每个attrs传过来的查询参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //attrs=2_5寸：6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.info(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                }else {
                    navVo.setNavName(s[0]);
                }
                //2.取消了面包屑以后，要跳转到哪个地方  ==>将请求地址的url里面的当前置空
                //拿到所有的查询条件，去掉当前
                //字符编码转换
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
            //品牌、分类
            if (param.getBrandId()!=null && param.getBrandId().size()>0) {
                List<SearchResult.NavVo> navs = result.getNavs();
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                navVo.setNavName("品牌");
                // TODO: 2020/9/23 远程查询所有品牌
                R r = productFeignService.brandsInfo(param.getBrandId());
                if (r.getCode() == 0) {
                    List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                    });
                    StringBuffer buffer = new StringBuffer();
                    String replace="";
                    for (BrandVo brandVo:brand){
                        buffer.append(brandVo.getBrandName()+";");
                        replace = replaceQueryString(param, brandVo.getBrandId()+"","brandId");
                    }

                    navVo.setNavValue(buffer.toString());
                    navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                }
                navs.add(navVo);
            }
            // TODO: 2020/9/23 分类  不需要导航取消
        }
        return result;

    }

    private String replaceQueryString(SearchParam param, String key, String value) {
        // 解决编码问题，前端参数使用UTF-8编码了
        String encode = null;
        encode = UriEncoder.encode(value);
//                try {
//                    encode = URLEncoder.encode(attr, "UTF-8");// java将空格转义成了+号
//                    encode = encode.replace("+", "%20");// 浏览器将空格转义成了%20，差异化处理，否则_queryString与encode匹配失败
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
        // TODO BUG，第一个参数不带&
        // 替换掉当前查询条件，剩下的查询条件即是回退地址
        String replace = param.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }

}