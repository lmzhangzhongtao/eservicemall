
#### 镜像抓取
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.6.2


### 运行命令

## docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.3.2



[//]: # (sudo docker run --name elasticsearch -p 9200:9200 -p 9300:9300 \)

[//]: # (-e "discovery.type=single-node" \)

[//]: # (-e ES_JAVA_OPTS="-Xms64m -Xmx128m" \)

[//]: # (-v /mydata/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \)

[//]: # (-v /mydata/elasticsearch/data:/usr/share/elasticsearch/data \)

[//]: # (-v /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins \)

[//]: # (-d elasticsearch)


docker network create elastic


docker run --name elasticsearch -p 9200:9200 -p 9300:9300 \
-e "discovery.type=single-node" \
-e ES_JAVA_OPTS="-Xms128m -Xmx512m" \
--net elastic \
-v /mydata/elasticsearch/data:/usr/share/elasticsearch/data \
-v /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins \
-d elasticsearch:8.6.2




修改密码
docker exec -it elasticsearch /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic -i


获取token
docker exec -it elasticsearch /usr/share/elasticsearch/bin/elasticsearch-create-enrollment-token -s kibana


复制证书



##### 安装Kibana(可视化界面)

### 运行命令
docker run --name kibana --net elastic  -p 5601:5601 -d kibana:8.6.2



## docker run --name kibana --net elastic -v /mydata/kibana/kibana.yml:/usr/share/kibana/config/kibana.yml -p 5601:5601 -d kibana:8.6.2



用户名:

elastic/elastic