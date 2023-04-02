package com.caspar.eservicemall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.SuggestMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.elasticsearch.sql.QueryResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.nacos.shaded.com.google.common.base.Supplier;
import com.caspar.eservicemall.common.utils.BeanMapUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;

@SpringBootTest
class EserviceSearchApplicationTests {

    @Autowired
	private ElasticsearchClient client;

	@Test
	public void createIndex() throws IOException {
		CreateIndexResponse products = client.indices().create(c -> c.index("db_idx5"));
		System.out.println(products.acknowledged());
		client.indices().create((c)->{
			return c.index("test");
		});

	}
//	//修改 _mapping 信息
//	@Test
//	public void modifyIndex() throws IOException {
//		PutMappingResponse response = client.indices().putMapping(typeMappingBuilder -> typeMappingBuilder
//				.index("users")
//				.properties("age", propertyBuilder -> propertyBuilder.integer(integerNumberPropertyBuilder -> integerNumberPropertyBuilder))
//		);
//		System.out.println("acknowledged={}"+response.acknowledged());
//	}



	@Test
	public void indexSingleDocumentByClassicBuilder() throws IOException{
		User user=new User();
		user.setUserName("casparZheng_class_builder");
		user.setAge(30);
		user.setGender("男");
		IndexRequest.Builder<User> indexReqBuilder = new IndexRequest.Builder<>();
		indexReqBuilder.index("users_class_builder");
		indexReqBuilder.id("1");
		indexReqBuilder.document(user);
		//IndexRequest<TDocument> request
		IndexResponse response = client.index(indexReqBuilder.build());
		System.out.println("Indexed with version " + response.version());
	}

    // 查询单个文档
	@Test
	public void readDocumentById() throws IOException{
		GetResponse<Map> response = client.get(builder -> builder.index("users").id("1"), Map.class);
		if (response.found()) {
		//	User user = response.source();
			System.out.println(response.source());
			BeanMapUtils<User> b=new BeanMapUtils<User>();
			User user=new User();
			b.mapToBean(response.source(),user);
			System.out.println("User name " + user.getUserName());
		} else {
			System.out.println ("User not found");
		}
	}

	// 查询所有文档
	@Test
	public void getDocAll() throws IOException {
		SearchResponse<Map> response = client.search(builder -> builder.index("users"), Map.class);
		System.out.println(response.toString());
	}

	@Test
	public void bulk() throws IOException {
		List<BulkOperation> list = new ArrayList<>();

		//批量新增
		for (int i = 0; i < 5; i++) {
			Map<String, Object> doc = new HashMap<>();
			doc.put("age", 30);
			doc.put("name", "李白" + i);
			doc.put("poems", "静夜思");
			doc.put("about", "字太白");
			doc.put("success", "创造了古代浪漫主义文学高峰、歌行体和七绝达到后人难及的高度");
			String id = 10 + i + "";
			list.add(new BulkOperation.Builder().create(builder -> builder.index(INDEX_NAME).id(id).document(doc)).build());
		}
		for (int i = 0; i < 5; i++) {
			Poet poet = new Poet(31, "杜甫" + i, "登高", "字子美", "唐代伟大的现实主义文学作家，唐诗思想艺术的集大成者");
			String id = 20 + i + "";
			list.add(new BulkOperation.Builder().create(builder -> builder.index(INDEX_NAME).id(id).document(poet)).build());
		}

		//批量删除
		list.add(new BulkOperation.Builder().delete(builder -> builder.index(INDEX_NAME).id("1")).build());
		list.add(new BulkOperation.Builder().delete(builder -> builder.index(INDEX_NAME).id("2")).build());

		BulkResponse response = client.bulk(builder -> builder.index(INDEX_NAME).operations(list));
		System.out.println(response.toString());
	}
   @Test
   public void bulkRequest() throws Exception{
	   List<BulkOperation> list = new ArrayList<>();
	   User user1=new User();
	   user1.setId("2");
	   user1.setGender("男");
	   user1.setUserName("郑成功");
	   user1.setAge(20);
	   User user2=new User();
	   user2.setId("3");
	   user2.setGender("女");
	   user2.setUserName("詹小梅");
	   user2.setAge(30);
	   List<User> users = new ArrayList<User>();
	   for (User user : users) {
		   list.add(new BulkOperation.Builder().create(builder -> builder.index("users").id(user.getId()).document(user)).build());
	   }
	   BulkResponse result = client.bulk(builder -> builder.index("users").operations(list));
	   // Log errors, if any
	   if (result.errors()) {
		   System.out.println("Bulk had errors");
		   for (BulkResponseItem item: result.items()) {
			   if (item.error() != null) {
				   System.out.println(item.error().reason());
			   }
		   }
	   }
   }
	/**
	 * range查询,范围查询
	 */
	@Test
	public void searchRange() throws IOException {
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.range(rangeQueryBuilder -> rangeQueryBuilder
										.field("age").gte(JsonData.of("20")).lt(JsonData.of("40"))))
				, Poet.class);
		System.out.println(response.toString());
	}
	private static final String INDEX_NAME = "poet-index";
	//  term/terms查询，对输入内容不做分词处理。
	@Test
	public void searchTerm() throws IOException {
		List<FieldValue> words = new ArrayList<>();
		//words.add(new FieldValue.Builder().stringValue("李白").build());
		words.add(new FieldValue.Builder().stringValue("杜甫").build());
		SearchResponse<Poet> response2 = client.search(searchRequestBuilder -> searchRequestBuilder
						.index("bulk_test_index")
						.query(queryBuilder -> queryBuilder
								.terms(termsQueryBuilder -> termsQueryBuilder
										.field("name").terms(termsQueryFieldBuilder -> termsQueryFieldBuilder.value(words))))
						.source(sourceConfigBuilder -> sourceConfigBuilder
								.filter(sourceFilterBuilder -> sourceFilterBuilder
										.excludes("about")))
						.from(0)
						.size(10)
				, Poet.class);
		System.out.println(response2.toString());
	}


	/**
	 * match查询，对输入内容先分词再查询
	 */
	@Test
	public void searchMatch() throws IOException {
		SearchResponse<Map> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.match(matchQueryBuilder -> matchQueryBuilder
										.field("success").query("思想")))
				, Map.class);
		System.out.println(response.toString());
	}

	/**
	 * multi_match查询,-
	 */
	@Test
	public void searchMultiMatch() throws IOException {
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.multiMatch(multiMatchQueryBuilder -> multiMatchQueryBuilder
										.fields("about", "success").query("思想")))
				, Poet.class);
		System.out.println(response.toString());
	}

	/**
	 * match_phrase 查询,匹配整个查询字符串
	 */
	@Test
	public void searchMatchPhrase() throws IOException {
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.matchPhrase(matchPhraseQueryBuilder -> matchPhraseQueryBuilder.field("success").query("文学作家")))
				, Poet.class);
		System.out.println(response.toString());
	}

	/**
	 * match_all 查询,查询所有
	 */
	@Test
	public void searchMatchAll() throws IOException {
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.matchAll(matchAllQueryBuilder -> matchAllQueryBuilder))
				, Poet.class);
		System.out.println(response.toString());
	}


	/**
	 * query_string 查询
	 */
	@Test
	public void searchQueryString() throws IOException {
		//类似 match
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.queryString(queryStringQueryBuilder -> queryStringQueryBuilder
										.defaultField("success").query("古典文学")))
				, Poet.class);
		System.out.println(response.toString());

		//类似 mulit_match
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.queryString(queryStringQueryBuilder -> queryStringQueryBuilder
										.fields("about", "success").query("古典文学")))
				, Poet.class);
		System.out.println(response.toString());

		//类似 match_phrase
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.queryString(queryStringQueryBuilder -> queryStringQueryBuilder
										.defaultField("success").query("\"文学作家\"")))
				, Poet.class);
		System.out.println(response.toString());

		//带运算符查询，运算符两边的词不再分词
		//查询同时包含 ”文学“ 和 ”伟大“ 的文档
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.queryString(queryStringQueryBuilder -> queryStringQueryBuilder
										.fields("success").query("文学 AND 伟大")))
				, Poet.class);
		System.out.println(response.toString());

		//等同上一个查询
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.queryString(queryStringQueryBuilder -> queryStringQueryBuilder
										.fields("success").query("文学 伟大").defaultOperator(Operator.And)))
				, Poet.class);
		System.out.println(response.toString());

		//查询 name 或 success 字段包含"文学"和"伟大"这两个单词，或者包含"李白"这个单词的文档。
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.queryString(queryStringQueryBuilder -> queryStringQueryBuilder
										.fields("name","success").query("(文学 AND 伟大) OR 高度")))
				, Poet.class);
		System.out.println(response.toString());
	}

	/**
	 * simple_query_string 查询,和query_string类似
	 * 不支持AND OR NOT，会当做字符串处理
	 * 使用 +替代AND,|替代OR,-替代NOT
	 */
	@Test
	public void searchSimpleQueryString() throws IOException {
		//查询同时包含 ”文学“ 和 ”伟大“ 的文档
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.simpleQueryString(simpleQueryStringQueryBuilder -> simpleQueryStringQueryBuilder
										.fields("success").query("文学 + 伟大")))
				, Poet.class);
		System.out.println(response.toString());
	}

	/**
	 * 模糊查询
	 */
	@Test
	public void searchFuzzy() throws IOException {
		//全文查询时使用模糊参数，先分词再计算模糊选项。
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.match(matchQueryBuilder -> matchQueryBuilder
										.field("success").query("思考").fuzziness("1")))
				, Poet.class);
		System.out.println(response.toString());

		//使用 fuzzy query，对输入不分词，直接计算模糊选项。
		SearchResponse<Poet> response2 = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.fuzzy(fuzzyQueryBuilder -> fuzzyQueryBuilder
										.field("success").fuzziness("1").value("理想")))
				, Poet.class);
		System.out.println(response2.toString());
	}

	/**
	 * bool查询,组合查询
	 */
	@Test
	public void searchBool() throws IOException {
		//查询 success 包含 “思想” 且 age 在 [20-40] 之间的文档
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.bool(boolQueryBuilder -> boolQueryBuilder
										.must(queryBuilder2 -> queryBuilder2
												.match(matchQueryBuilder -> matchQueryBuilder
														.field("success").query("思想"))
										)
										.must(queryBuilder2 -> queryBuilder2
												.range(rangeQueryBuilder -> rangeQueryBuilder
														.field("age").gte(JsonData.of("20")).lt(JsonData.of("40")))
										)
								)
						)
				, Poet.class);
		System.out.println(response.toString());

		//过滤出 success 包含 “思想” 且 age 在 [20-40] 之间的文档，不计算得分
		SearchResponse<Poet> response2 = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.bool(boolQueryBuilder -> boolQueryBuilder
										.filter(queryBuilder2 -> queryBuilder2
												.match(matchQueryBuilder -> matchQueryBuilder
														.field("success").query("思想"))
										)
										.filter(queryBuilder2 -> queryBuilder2
												.range(rangeQueryBuilder -> rangeQueryBuilder
														.field("age").gte(JsonData.of("20")).lt(JsonData.of("40")))
										)
								)
						)
				, Poet.class);
		System.out.println(response2.toString());
	}

	/**
	 * aggs查询,聚合查询
	 */
	@Test
	public void searchAggs() throws IOException {
		//求和
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.aggregations("age_sum", aggregationBuilder -> aggregationBuilder
								.sum(sumAggregationBuilder -> sumAggregationBuilder
										.field("age")))
				, Poet.class);
		System.out.println(response.toString());

		//类似 select count distinct(age) from poet-index
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.aggregations("age_count", aggregationBuilder -> aggregationBuilder
								.cardinality(cardinalityAggregationBuilder -> cardinalityAggregationBuilder.field("age")))
				, Poet.class);
		System.out.println(response.toString());

		//数量、最大、最小、平均、求和
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.aggregations("age_stats", aggregationBuilder -> aggregationBuilder
								.stats(statsAggregationBuilder -> statsAggregationBuilder
										.field("age")))
				, Poet.class);
		System.out.println(response.toString());

		//select name,count(*) from poet-index group by name
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.aggregations("name_terms", aggregationBuilder -> aggregationBuilder
								.terms(termsAggregationBuilder -> termsAggregationBuilder
										.field("name")))
				, Poet.class);
		System.out.println(response.toString());

		//select name,age,count(*) from poet-index group by name,age
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.aggregations("name_terms", aggregationBuilder -> aggregationBuilder
								.terms(termsAggregationBuilder -> termsAggregationBuilder
										.field("name")
								)
								.aggregations("age_terms", aggregationBuilder2 -> aggregationBuilder2
										.terms(termsAggregationBuilder -> termsAggregationBuilder
												.field("age")
										))
						)
				, Poet.class);
		System.out.println(response.toString());

		//类似 select avg(age) from poet-index where name='李白'
		response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.bool(boolQueryBuilder -> boolQueryBuilder
										.filter(queryBuilder2 -> queryBuilder2
												.term(termQueryBuilder -> termQueryBuilder
														.field("name").value("李白")))))
						.aggregations("ave_age", aggregationBuilder -> aggregationBuilder
								.avg(averageAggregationBuilder -> averageAggregationBuilder.field("age")))
				, Poet.class);
		System.out.println(response.toString());
	}

	/**
	 * suggest查询,推荐搜索, 报错
	 */
	@Test
	public void searchSuggest() throws IOException {
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.suggest(suggesterBuilder -> suggesterBuilder
								.suggesters("success_suggest", fieldSuggesterBuilder -> fieldSuggesterBuilder
										.text("思考")
										.term(termSuggesterBuilder -> termSuggesterBuilder
												.field("success")
												.suggestMode(SuggestMode.Always)
												.minWordLength(2)
										)
								)
						)
				, Poet.class);
		System.out.println(response.toString());
	}

	/**
	 * 高亮显示
	 */
	@Test
	public void searchHighlight() throws IOException {
		SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
						.index(INDEX_NAME)
						.query(queryBuilder -> queryBuilder
								.match(matchQueryBuilder -> matchQueryBuilder
										.field("success").query("思想")))
						.highlight(highlightBuilder -> highlightBuilder
								.preTags("<span color='red'>")
								.postTags("</span>")
								.fields("success", highlightFieldBuilder -> highlightFieldBuilder))
				, Poet.class);
		System.out.println(response.toString());
	}

	/**
	 * sql查询，报错
	 */
	@Test
	public void searchSql() throws IOException {
		QueryResponse response = client.sql().query(builder -> builder
				.format("json").query("SELECT * FROM \"" + INDEX_NAME + "\" limit 1"));
		System.out.println(response.toString());
	}



	@Test
	public void IndexData() throws IOException {
//        //索引数据
		User user=new User();
		user.setUserName("casparZheng");
		user.setAge(30);
		user.setGender("男");
		IndexResponse response = client.index(i -> i
				.index("users")
				.id("1")
				.document(user)
		);

		System.out.println(response);
	}






	@Test
	void contextLoads() throws IOException {
	}

	@Data
	@ToString
	public class User{
		private  String id;
		private String userName;
		private String gender;
		private Integer age;
	}
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Poet {
		private Integer age;
		private String name;
		private String poems;
		private String about;
		/**成就*/
		private String success;
	}
}

	@Data
	@ToString
	class Account
	{
		private int account_number;

		private int balance;

		private String firstname;

		private String lastname;

		private int age;

		private String gender;

		private String address;

		private String employer;

		private String email;

		private String city;

		private String state;

}
