1、使用 docker run 命令运行时

增加 --restart=always 参数即可

2、使用 docker-compose 命令运行时

在 yml 文件中，需要自启动的 service 下

增加 restart: always 项目即可

3、已运行的容器修改其自启动策略

执行命令：

docker update --restart=always 容器名或容器ID

docker container update --restart=【容器策略】 容器名称

       # no 容器退出时不重启容器

       # on-failure 只有在非零状态退出时才重新启动容器

              --restart=on-failure:【重启次数】

       # always 无论退出状态如何都
