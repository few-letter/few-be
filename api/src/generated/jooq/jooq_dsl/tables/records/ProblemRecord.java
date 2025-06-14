/*
 * This file is generated by jOOQ.
 */
package jooq.jooq_dsl.tables.records;


import java.time.LocalDateTime;

import jooq.jooq_dsl.tables.Problem;

import org.jooq.JSON;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProblemRecord extends UpdatableRecordImpl<ProblemRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>PROBLEM.ID</code>.
     */
    public ProblemRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.ID</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>PROBLEM.ARTICLE_ID</code>.
     */
    public ProblemRecord setArticleId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.ARTICLE_ID</code>.
     */
    public Long getArticleId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>PROBLEM.TITLE</code>.
     */
    public ProblemRecord setTitle(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.TITLE</code>.
     */
    public String getTitle() {
        return (String) get(2);
    }

    /**
     * Setter for <code>PROBLEM.CONTENTS</code>.
     */
    public ProblemRecord setContents(JSON value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.CONTENTS</code>.
     */
    public JSON getContents() {
        return (JSON) get(3);
    }

    /**
     * Setter for <code>PROBLEM.ANSWER</code>.
     */
    public ProblemRecord setAnswer(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.ANSWER</code>.
     */
    public String getAnswer() {
        return (String) get(4);
    }

    /**
     * Setter for <code>PROBLEM.EXPLANATION</code>.
     */
    public ProblemRecord setExplanation(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.EXPLANATION</code>.
     */
    public String getExplanation() {
        return (String) get(5);
    }

    /**
     * Setter for <code>PROBLEM.CREATOR_ID</code>.
     */
    public ProblemRecord setCreatorId(Long value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.CREATOR_ID</code>.
     */
    public Long getCreatorId() {
        return (Long) get(6);
    }

    /**
     * Setter for <code>PROBLEM.CREATED_AT</code>.
     */
    public ProblemRecord setCreatedAt(LocalDateTime value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.CREATED_AT</code>.
     */
    public LocalDateTime getCreatedAt() {
        return (LocalDateTime) get(7);
    }

    /**
     * Setter for <code>PROBLEM.DELETED_AT</code>.
     */
    public ProblemRecord setDeletedAt(LocalDateTime value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>PROBLEM.DELETED_AT</code>.
     */
    public LocalDateTime getDeletedAt() {
        return (LocalDateTime) get(8);
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
     * Create a detached ProblemRecord
     */
    public ProblemRecord() {
        super(Problem.PROBLEM);
    }

    /**
     * Create a detached, initialised ProblemRecord
     */
    public ProblemRecord(Long id, Long articleId, String title, JSON contents, String answer, String explanation, Long creatorId, LocalDateTime createdAt, LocalDateTime deletedAt) {
        super(Problem.PROBLEM);

        setId(id);
        setArticleId(articleId);
        setTitle(title);
        setContents(contents);
        setAnswer(answer);
        setExplanation(explanation);
        setCreatorId(creatorId);
        setCreatedAt(createdAt);
        setDeletedAt(deletedAt);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised ProblemRecord
     */
    public ProblemRecord(jooq.jooq_dsl.tables.pojos.Problem value) {
        super(Problem.PROBLEM);

        if (value != null) {
            setId(value.getId());
            setArticleId(value.getArticleId());
            setTitle(value.getTitle());
            setContents(value.getContents());
            setAnswer(value.getAnswer());
            setExplanation(value.getExplanation());
            setCreatorId(value.getCreatorId());
            setCreatedAt(value.getCreatedAt());
            setDeletedAt(value.getDeletedAt());
            resetChangedOnNotNull();
        }
    }
}
