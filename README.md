```shell
which java
ls -lrt /usr/bin/java
ls -lrt /etc/alternatives/java

#/usr/java/jdk1.8.0_221-amd64
```



# 1. 安装zookeeper

```shell
# 下载
wget https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz
# 解压
tar -zxvf zookeeper-3.4.14.tar.gz
# 重命名
mv apache-zookeeper-3.4.14.tar.gz zookeeper-3.4.14
# 重命名配置文件
cd zookeeper-3.4.14
cd conf
cp zoo_sample.cfg zoo.cfg
vi zoo.cfg
# 单机模式只修改数据存储目录, 其他默认
dataDir=/deploy/zookeeper_data
# 配置环境变量
vi /etc/profile
# 快捷键shift+g移到文件末尾
# 添加zk环境变量
ZK_HOME=/deploy/zookeeper-3.4.14
PATH=$ZK_HOME/bin:$PATH
# 启动(使用zkServer.sh start-foreground可以查看启动日志)(3.5.5启动提示找不到主类)
zkServer.sh start

```





# 2. 安装kafka

```shell
# kafka依赖zookeeper
# 下载
wget http://mirrors.tuna.tsinghua.edu.cn/apache/kafka/2.2.0/kafka_2.11-2.2.0.tgz
# 解压
tar -zxvf kafka_2.11-2.2.0.tgz
# 进入目录修改配置
cd kafka_2.11-2.2.0/config
vi server.properties
# 修改配置(其他保持默认,默认连接本机2181端口zookeeper) 
broker.id=0
listeners=PLAINTEXT://172.18.136.2:9092  #ifconfig获得的IP
advertised.listeners=PLAINTEXT://120.78.152.252:9092	#公网IP
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.dirs=/deploy/data/kafka-logs
num.partitions=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
zookeeper.connect=127.0.0.1:2181
zookeeper.connection.timeout.ms=6000
group.initial.rebalance.delay.ms=0


# 启动
# 进入bin目录
./kafka-server-start.sh ../config/server.properties
# 命令创建主题
./kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --partitions 1 --replication-factor 1 --topic testTopic
# 生产者
./kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic testTopic
# 消费者
./kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic testTopic


#创建主题
 ./kafka-console-consumer.sh --bootstrap-server PLAINTEXT://192.168.0.205:9092 --topic test2 --from-beginning


#kafka后台启动
nohup ./kafka-server-start.sh ../config/server.properties 2>&1 &


#创建主题时ip如果使用127.0.0.1可能导致连接不上

```





# 3. 安装hadoop

##### 下载地址

```shell
http://archive.cloudera.com/cdh5/cdh/5/
wget http://archive.cloudera.com/cdh5/cdh/5/hadoop-2.6.0-cdh5.9.3.tar.gz
```

##### master(以下配置master都指本机，类似xxx.master.com)

~~~shell
# 修改主机名, 将主机名改为master
vi /etc/hostname
# 修改hosts, 添加192.168.0.205 master
vi /etc/hosts
# ssh免密
ssh-keygen -t rsa
ssh-copy-id master
# 操作完成后查看authorized_keys文件
ls -a ~/.ssh
ssh master
~~~

##### 安装hadoop

```shell
# wget下载慢, 使用csdn下载的版本
# 解压
tar -zxvf hadoop-2.6.0.tar.gz
cd hadoop-2.6.0.tar.gz
cd etc/hadoop
# 修改hadoop-env.sh, 修改JAVA_HOME, 使用whereis java查看安装目录
vi hadoop-env.sh
export JAVA_HOME=/usr/java/jdk1.8.0_152
# 1.修改core-site.xml
vi core-site.xml
<configuration>
	<property>
		<name>fs.default.name</name>
		<value>hdfs://master:9000</value>
    </property>
    <property>
		<name>hadoop.tmp.dir</name>
		<value>/deploy/hadoop_data/tmp</value>
    </property>
</configuration>
# 2.修改hdfs-site.xml
vi hdfs-site.xml
<configuration>
	<property>
		<name>dfs.replication</name>
		<value>1</value>
    </property>
    <property>
		<name>dfs.permissions</name>
		<value>true</value>
    </property>
</configuration>
# 3.修改mapred-site.xml, 先复制模板
cp mapred-site.xml.template mapred-site.xml
vi mapred-site.xml
<configuration>
	<property>
		<name>mapreduce.framework.name</name>
		<value>yarn</value>
    </property>
    <property>
		<name>mapreduce.jobhistory.address</name>
		<value>master:10020</value>
    </property>
</configuration>
# 4.修改yarn-site.xml
vi yarn-site.xml
<configuration>
	<property>
		<name>yarn.resourcemanager.hostname</name>
		<value>master</value>
    </property>
    <property>
		<name>yar.nodemanager.aux-services</name>
		<value>mapreduce_shuffle</value>
    </property>
    <property>
		<name>mapreduce.job.ubertask.enable</name>
		<value>true</value>
    </property>
</configuration>
# 5.修改环境变量
vi /etc/profile
export JAVA_HOME=/usr/java/jdk1.8.0_152
export HADOOP_HOME=/deploy/hadoop-2.6.0
export PATH=.:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
# 让修改的环境变量生效
source /etc/profile
# 执行jps查看进程
jps
# 格式化
hadoop namenode -format
# 启动dfs
start-dfs.sh
# 启动yarn
start-yarn.sh
# 启动成功后访问web
http://192.168.0.205:50070
```





# 4. 安装hbase

```shell
# hbase依赖zookeeper
wget http://archive.cloudera.com/cdh5/cdh/5/hbase-1.2.0-cdh5.9.3.tar.gz
# (官方下载太慢, 使用csdn下载的版本)
tar -zxvf hbase-1.3.2-bin.tar.gz
cd hbase-1.3.2/conf
# 1.修改hbase-env.sh
export JAVA_HOME=/usr/java/jdk1.8.0_152
# 2.修改hbase-site.xm;
vi hbase-site.xml
<configuration>
	<property>
        <name>hbase.master.port</name>
        <value>16000</value>
    </property>

    <property>
        <name>hbase.master.info.port</name>
        <value>16010</value>
    </property>

    <property>
        <name>hbase.regionserver.port</name>
        <value>16201</value>
    </property>

    <property>
        <name>hbase.regionserver.info.port</name>
        <value>16301</value>
    </property>
	<property>
		<name>hbase.rootdir</name>
		<value>hdfs://master:9000/hbase</value>
    </property>
    <property>
		<name>hbase.cluster.distributed</name>
		<value>true</value>
    </property>
    <property>
		<name>hbase.zookeeper.quorum</name>
		<value>master</value>
    </property>
    <property>
		<name>dfs.replication</name>
		<value>1</value>
    </property>
</configuration>
# 3.配置regionservers, 将localhost改为master
vi regionservers
# 4.修改环境变量
vi /etc/profile
export JAVA_HOME=/usr/java/jdk1.8.0_152
export HADOOP_HOME=/deploy/hadoop-2.6.0
export HBASE_HOME=/deploy/hbase-1.3.2
export PATH=.:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$HBASE_HOME/bin:$PATH
source /etc/profile
# 5.启动hbase
start-hbase.sh
# 6.使用jps查看
jps
18866 Kafka
32482 HMaster
29156 NameNode
29414 SecondaryNameNode
29558 ResourceManager
29271 DataNode
4713 QuorumPeerMain
32601 HRegionServer
29642 NodeManager
# 7.web访问
http://192.168.0.205:16010

```





# 5. 安装elasticsearch

#### elasticsearch7.x要求Java11

```shell
# 下载elasticsearch7.2.0
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.2.0-linux-x86_64.tar.gz
# 解压
tar -zxvf elasticsearch-7.2.0-linux-x86_64.tar.gz
# 编辑config/elasticsearch.yml
vi elasticsearch.yml

#配置跨域
http.cors.enabled: true
http.cors.allow-origin: "*"

path.data: /deploy/elasticsearch_data
path.logs: /deploy/elasticsearch_log
network.host: 192.168.0.205
http.port: 9200
cluster.name: my_es
node.name: node1
action.auto_create_index: true #允许自动创建索引

bootstrap.memory_lock: true	#配置内存使用用交换分区


# 添加elasticsearch用户
useradd es
passwd es
# 给目录授权
chown -R es:es /deploy/elasticsearch_data
chown -R es:es /deploy/elasticsearch_log
chown -R es:es /deploy/elasticsearch-7.2.0
# 1.使用root用户编辑/etc/security/limits.conf
# 添加以下内容
* soft nofile 65536
* hard nofile 65536
* soft nproc 4096
* hard nproc 4096
# 2.root用户编辑vi /etc/sysctl.conf
# 添加下面配置：
vm.max_map_count=655360
#配置完成执行命令
sysctl -p
# 启动(切换到es用户下启动)
su es
/deploy/elasticsearch-7.2.0/bin/elasticsearch -d

# web访问
http://192.168.0.205:9200/

# 查看集群健康状态
http://120.77.176.63:9200/_cluster/health?pretty

# 修改index.max_result_window
curl -H "Content-Type: application/json" -XPUT 'http://39.108.94.127:9200/_all/_settings?preserve_existing=true' -d '{
  "index.max_result_window" : "600000"
}'



```



```java
// 设置fielddata, 修复聚合报错问题

curl -i -H "Content-Type:application/json" -XPUT 127.0.0.1:9200/your_index/_mapping/your_type/?pretty  -d'{"your_type":{"properties":{"your_field_name":{"type":"text","fielddata":true}}}}'

// eg:
curl -i -H "Content-Type:application/json" -XPUT 120.77.176.63:9200/visit/_mapping/visit/?pretty  -d'{"visit":{"properties":{"userId":{"type":"text","fielddata":true}}}}'
  
curl -i -H "Content-Type:application/json" -XPUT 120.77.176.63:9200/visit/_mapping/visit/?pretty  -d'{"visit":{"properties":{"lineId":{"type":"text","fielddata":true}}}}'
  
curl -i -H "Content-Type:application/json" -XPUT 120.77.176.63:9200/visit/_mapping/visit/?pretty  -d'{"visit":{"properties":{"activityId":{"type":"text","fielddata":true}}}}'
```





#### 安装elasticsearch-head插件

```shell
1.下载插件
2.解压到任意目录
3.npm install
4.nohup npm run start &

#访问地址
http://39.108.94.127:9100
```







#### 安装elasticsearch6.x

```shell
# 下载
wget  https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.4.2.tar.gz
# 解压
tar -zxvf elasticsearch-6.4.2.tar.gz
# 安装方法同7.x

# web访问
http://192.168.0.205:9200/


```



##### 查看集群状态

```shell
http://192.168.0.205:9200/_cluster/health?pretty=true
```



##### 查看mapping

```shell
# visit是索引

http://39.108.94.127:9200/visit/_mapping?pretty 
```



## 6.安装kibana

```shell
#下载地址
wget https://codeload.github.com/elastic/kibana/tar.gz/v6.4.2

#编辑kibana的配置文件
vim config/kibana.yml

server.port: 5601
server.host: "0.0.0.0"
elasticsearch.url: "http://39.108.94.127:9200"
kibana.index: ".kibana"

#启动
nohup /ops_server/kibana-6.1.4/bin/kibana  > "/ops_server/kibana-6.1.4/log/kibana_start.log" 2>&1 &  





#安装nodejs
#使用yum安装
yum install -y nodejs

#手动安装
wget https://nodejs.org/dist/v10.14.1/node-v10.14.1-linux-x64.tar.xz
tar -xvf node-v10.14.1-linux-x64.tar.xz
#现在 node 和 npm 还不能全局使用，做个链接。回到服务器跟目录下，执行命令：
ln -s /ops_server/download/node-v10.15.2-linux-x64/bin/node /usr/local/bin/node
ln -s /ops_server/download/node-v10.15.2-linux-x64/bin/npm /usr/local/bin/npm

ln -s /ops_server/download/node-v8.11.4-linux-x64/bin/ts-node /usr/local/bin/ts-node

#错误处理
#启动提示找不到ts-node, 找不到各种module
#进入kibana目录
npm install



#先添加index
#查询
nickName: *xx*  #模糊查询xx昵称

```





## 7.安装logstash

```shell
#编辑config/logstash.yml
node.name: host1    	#设置节点名称，一般写主机名
path.data: /usr/local/logstash/plugin-data    #创建logstash 和插件使用的持久化目录
config.reload.automatic: true    	#开启配置文件自动加载
config.reload.interval: 30s    		#定义配置文件重载时间周期
http.host: "39.108.94.127"    		#定义访问主机名，一般为域名或IP
```







## 8.安装fluentd

```shell
1.官方下载地址：https://www.fluentd.org/download
	fluentd 的运行环境为ruby
	ruby环境：
	yum install ruby
	
```

