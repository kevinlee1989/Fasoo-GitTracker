package com.example.demo.gitlab;

import com.example.demo.gitlab.dto.GitlabCoverageCounterResponse;
import com.example.demo.gitlab.dto.GitlabCoverageSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class GitlabCoverageService {

    private final GitlabClient gitlabClient;

    public GitlabCoverageSummaryResponse getCoverageSummary(Long jobId) {
        String xml = gitlabClient.getJacocoXmlReport(jobId);

        try {
            Document document = parseJacocoXml(xml);

            // 중요:
            // getElementsByTagName("counter") 쓰면 method/class/package counter까지 전부 가져옴.
            // 우리는 report 바로 아래의 전체 합산 counter만 읽어야 함.
            NodeList reportChildren = document.getDocumentElement().getChildNodes();

            return new GitlabCoverageSummaryResponse(
                    findCounter(reportChildren, "INSTRUCTION"),
                    findCounter(reportChildren, "BRANCH"),
                    findCounter(reportChildren, "LINE"),
                    findCounter(reportChildren, "METHOD"),
                    findCounter(reportChildren, "CLASS")
            );

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JaCoCo XML report", e);
        }
    }

    private Document parseJacocoXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // XML 외부 DTD / 외부 엔티티 로딩 방지
        // JaCoCo XML 안에 <!DOCTYPE ... "report.dtd"> 가 있어도 report.dtd 파일을 찾지 않게 함
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        // 외부 DTD / Schema 접근 차단
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        DocumentBuilder builder = factory.newDocumentBuilder();

        // 혹시 parser가 report.dtd를 요구해도 빈 값으로 처리
        builder.setEntityResolver((publicId, systemId) ->
                new InputSource(new StringReader(""))
        );

        return builder.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        );
    }

    private GitlabCoverageCounterResponse findCounter(NodeList nodes, String type) {
        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);

            // report 바로 아래의 <counter> 태그만 사용
            if (!"counter".equals(node.getNodeName())) {
                continue;
            }

            var attributes = node.getAttributes();
            var typeNode = attributes.getNamedItem("type");

            if (typeNode != null && type.equals(typeNode.getNodeValue())) {
                int missed = Integer.parseInt(attributes.getNamedItem("missed").getNodeValue());
                int covered = Integer.parseInt(attributes.getNamedItem("covered").getNodeValue());
                int total = missed + covered;

                double coverageRate = total == 0
                        ? 0.0
                        : Math.round(((double) covered / total * 100.0) * 100.0) / 100.0;

                return new GitlabCoverageCounterResponse(
                        type,
                        missed,
                        covered,
                        total,
                        coverageRate
                );
            }
        }

        return new GitlabCoverageCounterResponse(type, 0, 0, 0, 0.0);
    }
}