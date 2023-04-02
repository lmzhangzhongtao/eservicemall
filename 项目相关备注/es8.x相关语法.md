
#### 创建索引
PUT /es_db

# 查询索引:1
GET /es_db

# 查询索引是否存在
HEAD /es_db

# 删除索引
DELETE /es_db



# 添加文档
PUT /es_db/_doc/1
{
"name":"张三",
"age": 10,
"address":"深圳市宝安区海谷科技大厦"
}



# 查询文档
GET /es_db/_search

# 查询文档
GET /users_class_builder/_doc/1

# 条件查询  全文检索
GET /es_db/_search
{
"from":0,
"size":20,
"query": {
"match": {
"name": "张三"
}
}
}

# 条件查询
GET /es_db/_search?q=name:庆


### 条件查询
GET /bank_account/_search
{
"query": {
"match_all": {}
},
"sort": [
{
"account_number": {
"order": "desc"
}
}
]
}



#### 短语匹配
GET /_search
{
"query": {
"match_phrase": {
"message": "this is a test"
}
}
}



#### match  多字段匹配
GET /_search
{
"query": {
"multi_match" : {
"query":    "this is a test", 
"fields": [ "subject", "message" ]
}
}
}

#### boolean查询
GET /bank_account/_search
{
"query": {
"bool": {
"must": [
{
"match": {
"address": "Laurel"
}
},
{
"match": {
"gender": "F"
}
}
]
}
}
}


##### term
### 全文检索使用match,其他非text字段使用term进行精确检索










#### Aggregation聚合分析

##### 聚合分析 年龄的分布及平均值
GET /bank_account/_search
{
"query": {
"range": {
"age": {
"gte": 20,
"lte": 40
}
}
},
"aggs": {
"ageAgg": {
"terms": {
"field": "age",
"size": 10
}
},
"ageAvg":{
"avg": {
"field": "age"
}
}
}
}




##### 标准分词器
#### 文档  https://www.elastic.co/guide/en/elasticsearch/reference/8.6/indices-analyze.html

GET /_analyze
{
"analyzer" : "standard",
"text" : "Quick Brown Foxes!"
}


#### 使用smartcn分词器进行分词   analysis-smartcn-8.6.2
POST /_analyze
{
"analyzer": "smartcn",
"text": "我是中国人"
}




##### nested  当需要保持对象数组里面的对象属性的独立性，防止被扁平化处理，需要对对应字段添加nested标记。

### 文档： https://www.elastic.co/guide/en/elasticsearch/reference/8.7/nested.html