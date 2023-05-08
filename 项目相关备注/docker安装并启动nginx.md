


###启动命令

docker run -p 80:80 --name nginx \
-v /mydata/nginx/html:/usr/share/nginx/html \
-v /mydata/nginx/logs:/var/logs/nginx \
-v /mydata/nginx/conf:/etc/nginx \
-d nginx:latest


Docker实时查看日志命令

docker logs -f -t  -n=5 docker容器编码
-f 就是跟踪实时日志，-t用来显示docker日志的时间戳，-n就是显示docker容器的最后n行日志，
-n=5就是显示docker容器最后5行的日志，最后加上docker容器的编码就ok了。