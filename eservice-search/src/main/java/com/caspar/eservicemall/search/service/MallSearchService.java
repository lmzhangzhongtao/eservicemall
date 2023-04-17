package com.caspar.eservicemall.search.service;


import com.caspar.eservicemall.search.vo.SearchParam;
import com.caspar.eservicemall.search.vo.SearchResult;

public interface MallSearchService {
    /**
     *
     * @param param 检索的所有参数
     * @return 返回检索的所有结果
     */
    SearchResult search(SearchParam param);
}
