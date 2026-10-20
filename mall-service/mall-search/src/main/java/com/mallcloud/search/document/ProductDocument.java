package com.mallcloud.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * 商品索引文档。
 * <p>
 * 只放"检索与列表展示"需要的字段，详情页仍然回源商品服务：
 * 索引越小，重建越快，也越不容易和主库产生分歧。
 */
@Data
@Document(indexName = "mall_product")
public class ProductDocument {

    @Id
    private Long spuId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String subtitle;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private String brandId;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Keyword, index = false)
    private String mainImage;

    @Field(type = FieldType.Integer)
    private Integer sales;

    /** 1-上架，只有上架商品才会被检索到 */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 数据版本（毫秒时间戳），用于消费端幂等：旧版本消息直接丢弃 */
    @Field(type = FieldType.Long)
    private Long version;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
}
