/*
 * This file is generated by jOOQ.
 */
package jooq.jooq_dsl.tables.records;


import java.time.LocalDateTime;

import jooq.jooq_dsl.tables.ArticleMainCard;

import org.jooq.JSON;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ArticleMainCardRecord extends UpdatableRecordImpl<ArticleMainCardRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.ID</code>.
     */
    public ArticleMainCardRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.ID</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.TITLE</code>.
     */
    public ArticleMainCardRecord setTitle(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.TITLE</code>.
     */
    public String getTitle() {
        return (String) get(1);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.MAIN_IMAGE_URL</code>.
     */
    public ArticleMainCardRecord setMainImageUrl(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.MAIN_IMAGE_URL</code>.
     */
    public String getMainImageUrl() {
        return (String) get(2);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.CATEGORY_CD</code>.
     */
    public ArticleMainCardRecord setCategoryCd(Byte value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.CATEGORY_CD</code>.
     */
    public Byte getCategoryCd() {
        return (Byte) get(3);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.CREATED_AT</code>.
     */
    public ArticleMainCardRecord setCreatedAt(LocalDateTime value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.CREATED_AT</code>.
     */
    public LocalDateTime getCreatedAt() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.WRITER_ID</code>.
     */
    public ArticleMainCardRecord setWriterId(Long value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.WRITER_ID</code>.
     */
    public Long getWriterId() {
        return (Long) get(5);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.WRITER_EMAIL</code>.
     */
    public ArticleMainCardRecord setWriterEmail(String value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.WRITER_EMAIL</code>.
     */
    public String getWriterEmail() {
        return (String) get(6);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.WRITER_DESCRIPTION</code>.
     */
    public ArticleMainCardRecord setWriterDescription(JSON value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.WRITER_DESCRIPTION</code>.
     */
    public JSON getWriterDescription() {
        return (JSON) get(7);
    }

    /**
     * Setter for <code>ARTICLE_MAIN_CARD.WORKBOOKS</code>.
     */
    public ArticleMainCardRecord setWorkbooks(JSON value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>ARTICLE_MAIN_CARD.WORKBOOKS</code>.
     */
    public JSON getWorkbooks() {
        return (JSON) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ArticleMainCardRecord
     */
    public ArticleMainCardRecord() {
        super(ArticleMainCard.ARTICLE_MAIN_CARD);
    }

    /**
     * Create a detached, initialised ArticleMainCardRecord
     */
    public ArticleMainCardRecord(Long id, String title, String mainImageUrl, Byte categoryCd, LocalDateTime createdAt, Long writerId, String writerEmail, JSON writerDescription, JSON workbooks) {
        super(ArticleMainCard.ARTICLE_MAIN_CARD);

        setId(id);
        setTitle(title);
        setMainImageUrl(mainImageUrl);
        setCategoryCd(categoryCd);
        setCreatedAt(createdAt);
        setWriterId(writerId);
        setWriterEmail(writerEmail);
        setWriterDescription(writerDescription);
        setWorkbooks(workbooks);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised ArticleMainCardRecord
     */
    public ArticleMainCardRecord(jooq.jooq_dsl.tables.pojos.ArticleMainCard value) {
        super(ArticleMainCard.ARTICLE_MAIN_CARD);

        if (value != null) {
            setId(value.getId());
            setTitle(value.getTitle());
            setMainImageUrl(value.getMainImageUrl());
            setCategoryCd(value.getCategoryCd());
            setCreatedAt(value.getCreatedAt());
            setWriterId(value.getWriterId());
            setWriterEmail(value.getWriterEmail());
            setWriterDescription(value.getWriterDescription());
            setWorkbooks(value.getWorkbooks());
            resetChangedOnNotNull();
        }
    }
}
