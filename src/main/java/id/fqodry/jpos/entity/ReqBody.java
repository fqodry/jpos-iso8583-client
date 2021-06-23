package id.fqodry.jpos.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class ReqBody {
    private String hostname;
    private String port;
    private Map<String, String> message;
    private String messageString;
}
