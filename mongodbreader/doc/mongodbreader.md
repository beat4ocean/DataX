### Datax MongoDBReader
#### 1 快速介绍

MongoDBReader 插件利用 MongoDB 的java客户端MongoClient进行MongoDB的读操作。最新版本的Mongo已经将DB锁的粒度从DB级别降低到document级别，配合上MongoDB强大的索引功能，基本可以达到高性能的读取MongoDB的需求。
在针对字段频繁增加的mongodb表时，可以增加jsonType类型，将整个一行json数据保存为一个字段，进行导出。后续的ETL行为可以根据完整json进行灵活处理。

#### 2 实现原理

MongoDBReader通过Datax框架从MongoDB并行的读取数据，通过主控的JOB程序按照指定的规则对MongoDB中的数据进行分片，并行读取，然后将MongoDB支持的类型通过逐一判断转换成Datax支持的类型。

#### 3 功能说明
* 该示例从ODPS读一份数据到MongoDB。

	    {
	    "job": {
	        "setting": {
	            "speed": {
	                "channel": 2
	            }
	        },
	        "content": [
	            {
	                "reader": {
	                    "name": "mongodbreader",
	                    "parameter": {
	                        "address": ["127.0.0.1:27017"],
	                        "userName": "",
	                        "userPassword": "",
	                        "dbName": "tag_per_data",
	                        "authDb": "admin",
	                        "collectionName": "tag_data12",
	                        "query": "{\"dateUpdated\": {\"$gte\": {\"$date\": 1593014400000}, \"$lt\": {\"$date\": 1593100800000}}}",
	                        "column": [
	                            {
	                                "name": "unique_id",
	                                "type": "string"
	                            },
	                            {
	                                "name": "sid",
	                                "type": "string"
	                            },
	                            {
	                                "name": "user_id",
	                                "type": "string"
	                            },
	                            {
	                                "name": "auction_id",
	                                "type": "string"
	                            },
	                            {
	                                "name": "content_type",
	                                "type": "string"
	                            },
	                            {
	                                "name": "pool_type",
	                                "type": "string"
	                            },
	                            {
	                                "name": "frontcat_id",
	                                "type": "Array",
	                                "spliter": ""
	                            },
	                            {
	                                "name": "categoryid",
	                                "type": "Array",
	                                "spliter": ""
	                            },
	                            {
	                                "name": "gmt_create",
	                                "type": "string"
	                            },
	                            {
	                                "name": "taglist",
	                                "type": "Array",
	                                "spliter": " "
	                            },
	                            {
	                                "name": "property",
	                                "type": "string"
	                            },
	                            {
	                                "name": "scorea",
	                                "type": "int"
	                            },
	                            {
	                                "name": "scoreb",
	                                "type": "int"
	                            },
	                            {
	                                "name": "scorec",
	                                "type": "int"
	                            }
	                        ]
	                    }
	                },
	                "writer": {
	                    "name": "odpswriter",
	                    "parameter": {
	                        "project": "tb_ai_recommendation",
	                        "table": "jianying_tag_datax_read_test01",
	                        "column": [
	                            "unique_id",
	                            "sid",
	                            "user_id",
	                            "auction_id",
	                            "content_type",
	                            "pool_type",
	                            "frontcat_id",
	                            "categoryid",
	                            "gmt_create",
	                            "taglist",
	                            "property",
	                            "scorea",
	                            "scoreb"
	                        ],
	                        "accessId": "**************",
	                        "accessKey": "********************",
	                        "truncate": true,
	                        "odpsServer": "xxx/api",
	                        "tunnelServer": "xxx",
	                        "accountType": "aliyun"
	                    }
	                }
	            }
	        ]
	    }
        }
#### 4 参数说明

* address： MongoDB的数据地址信息，因为MonogDB可能是个集群，则ip端口信息需要以Json数组的形式给出。【必填】
* userName：MongoDB的用户名。【选填】
* userPassword： MongoDB的密码。【选填】
* collectionName： MonogoDB的集合名。【必填】
* column：MongoDB的文档列名。【必填】
* name：Column的名字。【必填】
* type：Column的类型。【选填】
* splitter：因为MongoDB支持数组类型，但是Datax框架本身不支持数组类型，所以mongoDB读出来的数组类型要通过这个分隔符合并成字符串。【选填】

#### 5 类型转换

| DataX 内部类型| MongoDB 数据类型    |
| -------- | -----  |
| Long     | int, Long |
| Double   | double |
| String   | string, array |
| Date     | date  |
| Boolean  | boolean |
| Bytes    | bytes |

#### 6 扫描mongo整行完整数据

	    {
	    "job": {
	        "setting": {
	            "speed": {
	                "channel": 2
	            }
	        },
	        "content": [
	            {
	                "reader": {
	                    "jsonType":true,
	                    "name": "mongodbreader",
	                    "parameter": {
	                        "address": ["127.0.0.1:27017"],
	                        "userName": "",
	                        "userPassword": "",
	                        "dbName": "tag_per_data",
	                        "authDb": "tag_per_data",
	                        "collectionName": "tag_data12",
	                        "column": [
	                            {
	                                "name": "_id",
	                                "type": "string"
	                            }
	                        ]
	                    }
	                },
	                "writer": {
	                    "name": "odpswriter",
	                    "parameter": {
	                        "project": "tb_ai_recommendation",
	                        "table": "jianying_tag_datax_read_test01",
	                        "column": [
	                            "id"
	                        ],
	                        "accessId": "**************",
	                        "accessKey": "********************",
	                        "truncate": true,
	                        "odpsServer": "xxx/api",
	                        "tunnelServer": "xxx",
	                        "accountType": "aliyun"
	                    }
	                }
	            }
	        ]
	    }
        }


#### 7 参数说明
* jsonType： 扫描mongo整行数据,true时，将忽略用户配置的column，将mongodb整行json数据不解析，直接输出完整json数据【选填】
* type： 字段的数据类型，建议用户对Document和Array数据类型都填写string，代码内部会自动判断数据类型。如果为string，Document和Array对象才能转为标准json输出，而不需要填写splitter参数。

#### 8 性能报告

|-| Sample采样切片| 通用skip方式切片    | 不切片
|------| -------- | -----  | -----
| 总数据量| 4T     | 4T | 4T
| 同步一天数据量| 8665601条(3.2G)   | 8665601条(3.2G) | 8665601条(3.2G)
| channel数| 5   | 5 | 1
| 切片时间| 4681毫秒    | 4131毫秒  | 0s
| 速度| 12.61MB/s  | 10.74MB/s | 4.80MB/s
| 总同步时间| 265s  | 293s | 659s
#### 9 测试报告