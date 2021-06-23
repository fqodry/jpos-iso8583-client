package id.fqodry.jpos.controller;

import id.fqodry.jpos.entity.EchoRequest;
import id.fqodry.jpos.entity.ReqBody;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.q2.iso.QMUX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class MainController {

    private SimpleDateFormat formatterBit7 = new SimpleDateFormat("MMddHHmmss");
    private SimpleDateFormat formatterBit12 = new SimpleDateFormat("HHmmss");
    private SimpleDateFormat formatterBit13 = new SimpleDateFormat("MMdd");

    @Autowired private QMUX qmux;

    @RequestMapping("/test")
    public Map<String, String> test(@RequestBody EchoRequest request) {
        Map<String, String> result = new HashMap<>();

        try {
            ISOMsg isoReq = new ISOMsg("0800");
            isoReq.set(7, formatterBit7.format(new Date()));
            isoReq.set(11, "123456");
            isoReq.set(48, request.getMessage());
            isoReq.set(70, "001");

            ISOMsg isoResp = qmux.request(isoReq, 20 * 1000);

            if(isoResp == null) {
                result.put("success", "false");
                result.put("error","timeout");
                return result;
            }

            String response = new String(isoResp.pack());

            result.put("success", "true");
            result.put("response_code", isoResp.getString(39));
            result.put("raw_message", response);
        } catch(ISOException | NullPointerException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    @RequestMapping("/in-json")
    public Map<String, String> inJson(@RequestBody ReqBody request) {
        Map<String, String> result = new HashMap<>();

        try {
            ISOMsg isoReq = this.jsonReqHandler(request);
            // isoReq.set(7, formatterBit7.format(new Date()));
            isoReq.set(12, formatterBit12.format(new Date()));
            isoReq.set(13, formatterBit13.format(new Date()));

            ISOMsg isoResp = qmux.request(isoReq, 20 * 1000);

            if(isoResp == null) {
                log.error("request timeout...");

                result.put("success", "false");
                result.put("error","timeout");
                return result;
            }

            String response = new String(isoResp.pack());

            result.put("success", "true");
            result.put("response_code", isoResp.getString(39));
            result.put("raw_message", response);
        } catch (ISOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @RequestMapping("/in-string")
    public Map<String, String> inString(@RequestBody ReqBody request) throws ISOException {
        Map<String, String> result = new HashMap<>();

        try {
            ISOMsg isoReq = this.stringReqHandler(request.getMessageString());
            // isoReq.set(7, formatterBit7.format(new Date()));
            isoReq.set(12, formatterBit12.format(new Date()));
            isoReq.set(13, formatterBit13.format(new Date()));

            ISOMsg isoResp = qmux.request(isoReq, 20 * 1000);

            if(isoResp == null) {
                result.put("success", "false");
                result.put("error","timeout");
                return result;
            }

            String response = new String(isoResp.pack());

            result.put("success", "true");
            result.put("response_code", isoResp.getString(39));
            result.put("raw_message", response);
        } catch (ISOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private ISOMsg jsonReqHandler(ReqBody request) {
        ISOMsg isoReq = new ISOMsg();

        try {
            request.getMessage().forEach((k, v) -> {
                if("mti".equalsIgnoreCase(k)) {
                    try {
                        isoReq.setMTI(v);
                    } catch (ISOException e) {
                        e.printStackTrace();
                    }
                } else {
                    isoReq.set(k, v);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return isoReq;
    }

    private ISOMsg stringReqHandler(String message) {
        ISOMsg isoMsg = new ISOMsg();

        try {
            isoMsg.setPackager(new GenericPackager("cfg/cimb-packager.xml"));
            isoMsg.unpack(message.getBytes());
        } catch (ISOException | NullPointerException e) {
            e.printStackTrace();
        }

        return isoMsg;
    }
}
