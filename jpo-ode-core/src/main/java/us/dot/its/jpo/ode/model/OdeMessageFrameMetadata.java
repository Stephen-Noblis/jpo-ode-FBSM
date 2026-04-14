package us.dot.its.jpo.ode.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.ode.plugin.ServiceRequest;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OdeMessageFrameMetadata extends OdeLogMetadata {

  public enum Source {
    RSU, V2X, MMITSS, EV, RV, SAT, SNMP, NA, UNKNOWN
  }

  private Source source;
  private String originIp;

  @JsonProperty("request")
  private ServiceRequest request;

  @JsonProperty("isCertPresent")
  private boolean isCertPresent;

  private String ieee1609dot2DecodedXml;
  private String ieee1609dot2DecodeError;
  private String ieee1609dot2DecodedJson;

  public OdeMessageFrameMetadata(OdeMsgPayload<?> payload) {
    super(payload);
  }

  public OdeMessageFrameMetadata(Source source) {
    this.source = source;
  }
}