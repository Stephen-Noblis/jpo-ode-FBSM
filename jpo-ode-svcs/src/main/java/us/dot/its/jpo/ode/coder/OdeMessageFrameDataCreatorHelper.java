package us.dot.its.jpo.ode.coder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.asn.j2735.r2024.MessageFrame.MessageFrame;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;
import us.dot.its.jpo.ode.model.OdeMessageFrameMetadata;
import us.dot.its.jpo.ode.model.OdeMessageFramePayload;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.plugin.ServiceRequest;

/**
 * Helper class for creating OdeMessageFrameData objects from consumed data.
 */
@Slf4j
public class OdeMessageFrameDataCreatorHelper {

  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

  private OdeMessageFrameDataCreatorHelper() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  public static OdeMessageFrameData createOdeMessageFrameData(
      String consumedData,
      XmlMapper simpleXmlMapper) throws JsonProcessingException {

    JsonNode rootNode = simpleXmlMapper.readTree(consumedData);

    JsonNode metadataNode = rootNode.get("metadata");
    ServiceRequest request = null;

    if (metadataNode instanceof ObjectNode object) {
      if (object.has("request")) {
        JsonNode requestNode = object.get("request");

        if (requestNode != null && requestNode.isObject() && requestNode.size() > 0) {
          String xmlBack = simpleXmlMapper.writeValueAsString(requestNode);
          request = simpleXmlMapper.readValue(xmlBack, ServiceRequest.class);
        }

        object.remove("request");
      }
    }

    OdeMessageFrameMetadata metadata =
        simpleXmlMapper.treeToValue(metadataNode, OdeMessageFrameMetadata.class);

    metadata.setRequest(request);
    metadata.setEncodings(null);

    if (metadata.getIeee1609dot2DecodedXml() != null
        && !metadata.getIeee1609dot2DecodedXml().isBlank()) {

      JsonNode ieeeJsonNode =
          simpleXmlMapper.readTree(metadata.getIeee1609dot2DecodedXml());

      metadata.setIeee1609dot2DecodedJson(
          JSON_MAPPER.writeValueAsString(ieeeJsonNode));
    }

    if (metadata.getReceivedMessageDetails() != null
        && metadata.getReceivedMessageDetails().getRxSource() == null) {
      metadata.getReceivedMessageDetails().setRxSource(RxSource.NA);
    }

    if (metadata.getSchemaVersion() <= 4) {
      metadata.setReceivedMessageDetails(null);
    }

    JsonNode messageFrameNode =
        rootNode.get("payload").get("data").get("MessageFrame");

    MessageFrame<?> messageFrame =
        simpleXmlMapper.convertValue(messageFrameNode, MessageFrame.class);

    OdeMessageFramePayload payload = new OdeMessageFramePayload(messageFrame);

    return new OdeMessageFrameData(metadata, payload);
  }
}