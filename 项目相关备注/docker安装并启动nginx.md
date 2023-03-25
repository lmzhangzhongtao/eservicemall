


###启动命令

docker run -p 80:80 --name nginx \
-v /mydata/nginx/html:/usr/share/nginx/html \
-v /mydata/nginx/logs:/var/logs/nginx \
-v /mydata/nginx/conf:/etc/nginx \
-d nginx:latest
