#指定基础镜像
FROM openjdk:8-jdk-alpine
#设置环境变量
ENV APP_PATH=/app
#设置工作目录
WORKDIR $APP_PATH
#将jar包导入新的镜像
ADD search-backend-0.0.1-SNAPSHOT.jar $APP_PATH/apps.jar
#暴露端口
EXPOSE 8102
ENTRYPOINT ["java","-jar"]
CMD ["apps.jar"]
