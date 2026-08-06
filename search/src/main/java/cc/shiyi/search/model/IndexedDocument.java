package cc.shiyi.search.model;

import lombok.Data;

import java.util.Date;

@Data
public class IndexedDocument {
    Long docId;
    String docTitle;
    Long paragraphId;
    String paragraphTitle;
    String content;
    String url;
    Date createdTime;
}
