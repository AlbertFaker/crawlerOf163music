package com.crawl.music.dao;

import com.crawl.core.dao.ConnectionManager;
import com.crawl.music.entity.Music;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class MusicDAO {
    private static Logger logger = LoggerFactory.getLogger(MusicDAO.class);

    /**
     * 数据库表初始化，创建数据库表。
     * 如果存在的话，则不创建
     * @param cn
     */
    public static void DBTablesInit(Connection cn){
        ResultSet rs;
        Properties p = new Properties();
        try {
            //加载properties文件
            p.load(MusicDAO.class.getResourceAsStream("/config.properties"));
            rs = cn.getMetaData().getTables(null, null, "url", null);
            Statement st = cn.createStatement();
            //不存在url表
            if(!rs.next()){
                //创建url表
                st.execute(p.getProperty("createCommentTable"));
                logger.info("url表创建成功");
            }
            else{
                logger.info("url表已存在");
            }
            rs = cn.getMetaData().getTables(null, null, "music", null);
            //不存在user表
            if(!rs.next()){
                //创建user表
                st.execute(p.getProperty("createUserTable"));
                logger.info("music表创建成功");
            }
            else{
                logger.info("music表已存在");
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 判断该数据库中是否存在该用户
     * @param sql 判断该sql数据库中是否存在
     * @return
     */
    private synchronized static boolean isExistRecord(String sql) throws SQLException {
        int num = 0;
        PreparedStatement pstmt;
        pstmt = ConnectionManager.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()){
            num = rs.getInt("count(*)");
        }
        rs.close();
        pstmt.close();
//        ConnectionManager.close();
        if(num == 0){
            return false;
        }else{
            return true;
        }
    }
    /**
     * user 插入数据库
     * @param u
     * @throws SQLException
     */
    public synchronized static boolean insertUser(Music u){
        try {
            if (isExistUser(u.getId())){
                return false;
            }
            String column = "location,business,sex,employment,username,url,agrees,thanks,asks," +
                    "answers,posts,followees,followers,hashId,education,user_token";
            String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            String sql = "insert into user (" + column + ") values(" +values+")";
            PreparedStatement pstmt;
            pstmt = ConnectionManager.getConnection().prepareStatement(sql);
            pstmt.setString(1,u.getSinger());
            pstmt.setString(2,u.getAlbum());
            pstmt.setString(5,u.getName());
            pstmt.setString(6,u.getUrl());
            pstmt.setInt(12,u.getCommentCount());
            pstmt.setString(14,u.getId());
            pstmt.executeUpdate();
            pstmt.close();
            logger.info("插入数据库成功---" + u.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
//            ConnectionManager.close();
        }
        return true;
    }

    /**
     * 是否存在该用户
     * @param userToken
     * @return
     */
    public synchronized static boolean isExistUser(String userToken){
        String isContainSql = "select count(*) from user WHERE user_token='" + userToken + "'";
        try {
            if(isExistRecord(isContainSql)){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 将访问过的url插入数据库
     * @param md5Url 经过md5处理后的url
     * @return
     * @throws SQLException
     */
    public synchronized static boolean insertUrl(String md5Url){
        String isContainSql = "select count(*) from url WHERE md5_url ='" + md5Url + "'";
        try {
            if(isExistRecord(isContainSql)){
                logger.debug("数据库已经存在该url---" + md5Url);
                return false;
            }
            String sql = "insert into url (md5_url) values( ?)";
            PreparedStatement pstmt;
            pstmt = ConnectionManager.getConnection().prepareStatement(sql);
            pstmt.setString(1,md5Url);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.debug("url插入成功---");
        return true;
    }

    /**
     * 清空表
     * @param cn
     * @throws SQLException
     */
    public synchronized static void deleteUrlTable(Connection cn){
        String sql = "DELETE FROM url";
        PreparedStatement pstmt = null;
        try {
            pstmt = cn.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt.close();
            logger.info("url表删除成功---");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
