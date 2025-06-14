/*
 * This file is generated by jOOQ.
 */
package jooq.jooq_dsl.tables.records;


import java.time.LocalDateTime;

import jooq.jooq_dsl.tables.Workbook;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WorkbookRecord extends UpdatableRecordImpl<WorkbookRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>WORKBOOK.ID</code>.
     */
    public WorkbookRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>WORKBOOK.ID</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>WORKBOOK.TITLE</code>.
     */
    public WorkbookRecord setTitle(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>WORKBOOK.TITLE</code>.
     */
    public String getTitle() {
        return (String) get(1);
    }

    /**
     * Setter for <code>WORKBOOK.MAIN_IMAGE_URL</code>.
     */
    public WorkbookRecord setMainImageUrl(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>WORKBOOK.MAIN_IMAGE_URL</code>.
     */
    public String getMainImageUrl() {
        return (String) get(2);
    }

    /**
     * Setter for <code>WORKBOOK.CATEGORY_CD</code>.
     */
    public WorkbookRecord setCategoryCd(Byte value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>WORKBOOK.CATEGORY_CD</code>.
     */
    public Byte getCategoryCd() {
        return (Byte) get(3);
    }

    /**
     * Setter for <code>WORKBOOK.DESCRIPTION</code>.
     */
    public WorkbookRecord setDescription(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>WORKBOOK.DESCRIPTION</code>.
     */
    public String getDescription() {
        return (String) get(4);
    }

    /**
     * Setter for <code>WORKBOOK.CREATED_AT</code>.
     */
    public WorkbookRecord setCreatedAt(LocalDateTime value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>WORKBOOK.CREATED_AT</code>.
     */
    public LocalDateTime getCreatedAt() {
        return (LocalDateTime) get(5);
    }

    /**
     * Setter for <code>WORKBOOK.DELETED_AT</code>.
     */
    public WorkbookRecord setDeletedAt(LocalDateTime value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>WORKBOOK.DELETED_AT</code>.
     */
    public LocalDateTime getDeletedAt() {
        return (LocalDateTime) get(6);
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
     * Create a detached WorkbookRecord
     */
    public WorkbookRecord() {
        super(Workbook.WORKBOOK);
    }

    /**
     * Create a detached, initialised WorkbookRecord
     */
    public WorkbookRecord(Long id, String title, String mainImageUrl, Byte categoryCd, String description, LocalDateTime createdAt, LocalDateTime deletedAt) {
        super(Workbook.WORKBOOK);

        setId(id);
        setTitle(title);
        setMainImageUrl(mainImageUrl);
        setCategoryCd(categoryCd);
        setDescription(description);
        setCreatedAt(createdAt);
        setDeletedAt(deletedAt);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised WorkbookRecord
     */
    public WorkbookRecord(jooq.jooq_dsl.tables.pojos.Workbook value) {
        super(Workbook.WORKBOOK);

        if (value != null) {
            setId(value.getId());
            setTitle(value.getTitle());
            setMainImageUrl(value.getMainImageUrl());
            setCategoryCd(value.getCategoryCd());
            setDescription(value.getDescription());
            setCreatedAt(value.getCreatedAt());
            setDeletedAt(value.getDeletedAt());
            resetChangedOnNotNull();
        }
    }
}
