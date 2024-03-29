package com.PIVAs.services;

import com.PIVAs.constant.OrdersCheckConst;
import com.PIVAs.entity.ORDERS_CHECK;
import com.PIVAs.util.DBUtil;
import com.PIVAs.util.ReplaceNullStringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PUSH_JMPZ_ORDERS_CHECK {
    private static final Logger LOG= LoggerFactory.getLogger(PUSH_JMPZ_ORDERS_CHECK.class);
    Connection conn=null;
    Document document=null;
    CallableStatement proc=null;
    ORDERS_CHECK ordersCheck=new ORDERS_CHECK();
    private String sourceSystem,messageId;
    StringBuilder errMessage=new StringBuilder("");
    public String PUSH_JMPZ_ORDERS_CHECK(Document requestxml){
        try {
            conn = DBUtil.getConnection();
            Element root=requestxml.getRootElement();
            //获取入参SourceSystem的节点的值
            Element sourcesystem=root.element("Header").element("SourceSystem");
            sourceSystem= ReplaceNullStringUtil.replaceNullString(sourcesystem.getText());
            //获取入参MessageID的值
            Element messageid=root.element("Header").element("MessageID");
            messageId=ReplaceNullStringUtil.replaceNullString(messageid.getText());
            document = DocumentHelper.createDocument();
            document.setXMLEncoding("utf-8");
            Element Request = document.addElement("Request");
            Element Header = Request.addElement("Header");
            Element SourceSystem = Header.addElement("SourceSystem");
            SourceSystem.setText(sourceSystem);
            Element MessageID = Header.addElement("MessageID");
            MessageID.setText(messageId);
            Element Body = Request.addElement("Body");
            Element CODE = Body.addElement("CODE");
            CODE.setText(ReplaceNullStringUtil.replaceNullString("0"));
            Element MESSAGE = Body.addElement("MESSAGE");
            MESSAGE.setText("成功");
            Element SEQID = Body.addElement("SEQID");
            SEQID.setText(ReplaceNullStringUtil.replaceNullString(root.element("Body").element("rows").element("row").element("SEQID").getText()));
            List<Element> elebody=root.element("Body").element("rows").elements("row");
            //获取系统当前时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowTime=sdf.format(new Date());
            for (Element elebodys:elebody ) {
                ordersCheck.setPATIENT_ID(ReplaceNullStringUtil.replaceNullString(elebodys.element("PATIENT_ID").getText()));
                ordersCheck.setVISIT_ID(ReplaceNullStringUtil.replaceNullString(elebodys.element("VISIT_ID").getText()));
                ordersCheck.setORDER_ID(ReplaceNullStringUtil.replaceNullString(elebodys.element("ORDER_ID").getText()));
                ordersCheck.setSHBZ(ReplaceNullStringUtil.replaceNullString(elebodys.element("SHBZ").getText()));
                ordersCheck.setSHENGFANGZT(ReplaceNullStringUtil.replaceNullString(elebodys.element("SHENGFANGZT").getText()));
                ordersCheck.setSEQID(ReplaceNullStringUtil.replaceNullString(elebodys.element("SEQID").getText()));
                try {
                    proc = conn.prepareCall("{call PUSH_JMPZ_ORDERS_CHECK_INS(?,?,?,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?)}");
                    proc.setString(1, ordersCheck.getPATIENT_ID());
                    proc.setString(2, ordersCheck.getVISIT_ID());
                    proc.setString(3, ordersCheck.getORDER_ID());
                    proc.setString(4, ordersCheck.getSHBZ());
                    proc.setString(5, ordersCheck.getSHENGFANGZT());
                    proc.setString(6,nowTime);
                    proc.setInt(7, OrdersCheckConst.OrdersCheckEnum.CREATE.getValue());
                    proc.setString(8, ordersCheck.getSEQID());
                    proc.execute();
                } catch (Exception e) {
                    errMessage.append(e.getMessage());
                    fail();
                }
            }
        } catch (IOException e1) {
            return "数据库连接失败！";
        }finally {
            DBUtil.close(proc);
            DBUtil.close(conn);
        }
        LOG.error(errMessage.toString());
        return document.asXML();
    }
    public void fail(){
        Element Request = document.addElement("Request");
        Element Header = Request.addElement("Header");
        Element SourceSystem = Header.addElement("SourceSystem");
        SourceSystem.setText(sourceSystem);
        Element MessageID = Header.addElement("MessageID");
        MessageID.setText(messageId);
        Element Body = Request.addElement("Body");
        Element CODE = Body.addElement("CODE");
        CODE.setText(ReplaceNullStringUtil.replaceNullString("0"));
        Element MESSAGE = Body.addElement("MESSAGE");
        MESSAGE.setText("失败");
        Element SEQID = Body.addElement("SEQID");
        SEQID.setText(ordersCheck.getSEQID());
        LOG.error(""+errMessage);
    }
}
