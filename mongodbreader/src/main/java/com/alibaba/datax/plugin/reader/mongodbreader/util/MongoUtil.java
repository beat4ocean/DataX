package com.alibaba.datax.plugin.reader.mongodbreader.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.reader.mongodbreader.KeyConstant;
import com.alibaba.datax.plugin.reader.mongodbreader.MongoDBReaderErrorCode;
import com.mongodb.*;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MongoUtil.class);

    public static MongoClient initMongoClient(Configuration conf) {
        List addressList = conf.getList(KeyConstant.MONGO_ADDRESS);

        if (addressList != null && !addressList.isEmpty()) {
            try {
                return new MongoClient(parseServerAddress(addressList));
            } catch (UnknownHostException var3) {
                throw DataXException.asDataXException(MongoDBReaderErrorCode.ILLEGAL_ADDRESS, "不合法的地址");
            } catch (NumberFormatException var4) {
                throw DataXException.asDataXException(MongoDBReaderErrorCode.ILLEGAL_VALUE, "不合法参数");
            } catch (Exception var5) {
                throw DataXException.asDataXException(MongoDBReaderErrorCode.UNEXCEPT_EXCEPTION, "未知异常");
            }
        } else {
            throw DataXException.asDataXException(MongoDBReaderErrorCode.ILLEGAL_VALUE, "不合法参数");
        }
    }

    public static MongoClient initCredentialMongoClient(Configuration conf, String userName, String password, String database) {
        List addressList = conf.getList(KeyConstant.MONGO_ADDRESS);
        if (!isHostPortPattern(addressList)) {
            throw DataXException.asDataXException(MongoDBReaderErrorCode.ILLEGAL_VALUE, "不合法参数");
        } else {
            try {

                String address = org.apache.commons.lang3.StringUtils.join(addressList.toArray(), ",");
                MongoClient mongoClient;
                String uri;

                try {
                    // 优先走默认方式创建MongoClient
                    MongoCredential credential1 = MongoCredential.createCredential(userName, database, password.toCharArray());
                    mongoClient = new MongoClient(parseServerAddress(addressList), Arrays.asList(new MongoCredential[]{credential1}));
                    Document document = mongoClient.getDatabase(database).runCommand(new Document("ping", 1));

                } catch (Exception e) {
                    mongoClient = null;
                }
                if (null == mongoClient) {
                    uri = "mongodb://" + userName + ":" + password + "@" + address + "/?authMechanism=SCRAM-SHA-1";
                    MongoClientURI mongoClientURI = new MongoClientURI(uri);
                    mongoClient = new MongoClient(mongoClientURI);
                    Document document = mongoClient.getDatabase(database).runCommand(new Document("ping", 1));
                }
                // 设置优先读取副本
                mongoClient.setReadPreference(com.mongodb.ReadPreference.secondaryPreferred());
                return mongoClient;

            } catch (NumberFormatException var15) {
                throw DataXException.asDataXException(MongoDBReaderErrorCode.ILLEGAL_VALUE, "不合法参数");
            } catch (Exception var16) {
                throw DataXException.asDataXException(MongoDBReaderErrorCode.UNEXCEPT_EXCEPTION, "未知异常");
            }
        }
    }

    /**
     * 判断地址类型是否符合要求
     *
     * @param addressList
     * @return
     */
    private static boolean isHostPortPattern(List addressList) {
        Iterator var1 = addressList.iterator();

        Object address;
        String regex;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            address = var1.next();
            regex = "(\\S+):([0-9]+)";
        } while (((String) address).matches(regex));

        return false;
    }

    /**
     * 转换为mongo地址协议
     *
     * @param rawAddressList
     * @return
     */
    private static List parseServerAddress(List<Object> rawAddressList) throws UnknownHostException {
        ArrayList addressList = new ArrayList();
        for (Object address : rawAddressList) {
            String[] tempAddress = ((String) address).split(":");
            for (String temp : tempAddress) {
                LOG.debug("address {},temp {}", address, temp);
            }
            ServerAddress e = new ServerAddress(tempAddress[0], Integer.parseInt(tempAddress[1]));
            addressList.add(e);
        }


        return addressList;
    }
}
