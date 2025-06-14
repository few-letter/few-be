/*
 * This file is generated by jOOQ.
 */
package jooq.jooq_dsl.tables;


import java.time.LocalDateTime;
import java.util.Collection;

import jooq.jooq_dsl.DefaultSchema;
import jooq.jooq_dsl.Keys;
import jooq.jooq_dsl.tables.records.SendArticleEventHistoryRecord;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SendArticleEventHistory extends TableImpl<SendArticleEventHistoryRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>SEND_ARTICLE_EVENT_HISTORY</code>
     */
    public static final SendArticleEventHistory SEND_ARTICLE_EVENT_HISTORY = new SendArticleEventHistory();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SendArticleEventHistoryRecord> getRecordType() {
        return SendArticleEventHistoryRecord.class;
    }

    /**
     * The column <code>SEND_ARTICLE_EVENT_HISTORY.ID</code>.
     */
    public final TableField<SendArticleEventHistoryRecord, Long> ID = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>SEND_ARTICLE_EVENT_HISTORY.MEMBER_ID</code>.
     */
    public final TableField<SendArticleEventHistoryRecord, Long> MEMBER_ID = createField(DSL.name("MEMBER_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>SEND_ARTICLE_EVENT_HISTORY.ARTICLE_ID</code>.
     */
    public final TableField<SendArticleEventHistoryRecord, Long> ARTICLE_ID = createField(DSL.name("ARTICLE_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>SEND_ARTICLE_EVENT_HISTORY.MESSAGE_ID</code>.
     */
    public final TableField<SendArticleEventHistoryRecord, String> MESSAGE_ID = createField(DSL.name("MESSAGE_ID"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>SEND_ARTICLE_EVENT_HISTORY.EVENT_TYPE_CD</code>.
     */
    public final TableField<SendArticleEventHistoryRecord, Byte> EVENT_TYPE_CD = createField(DSL.name("EVENT_TYPE_CD"), SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>SEND_ARTICLE_EVENT_HISTORY.SEND_TYPE_CD</code>.
     */
    public final TableField<SendArticleEventHistoryRecord, Byte> SEND_TYPE_CD = createField(DSL.name("SEND_TYPE_CD"), SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>SEND_ARTICLE_EVENT_HISTORY.CREATED_AT</code>.
     */
    public final TableField<SendArticleEventHistoryRecord, LocalDateTime> CREATED_AT = createField(DSL.name("CREATED_AT"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.LOCALDATETIME)), this, "");

    private SendArticleEventHistory(Name alias, Table<SendArticleEventHistoryRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private SendArticleEventHistory(Name alias, Table<SendArticleEventHistoryRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>SEND_ARTICLE_EVENT_HISTORY</code> table reference
     */
    public SendArticleEventHistory(String alias) {
        this(DSL.name(alias), SEND_ARTICLE_EVENT_HISTORY);
    }

    /**
     * Create an aliased <code>SEND_ARTICLE_EVENT_HISTORY</code> table reference
     */
    public SendArticleEventHistory(Name alias) {
        this(alias, SEND_ARTICLE_EVENT_HISTORY);
    }

    /**
     * Create a <code>SEND_ARTICLE_EVENT_HISTORY</code> table reference
     */
    public SendArticleEventHistory() {
        this(DSL.name("SEND_ARTICLE_EVENT_HISTORY"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public Identity<SendArticleEventHistoryRecord, Long> getIdentity() {
        return (Identity<SendArticleEventHistoryRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<SendArticleEventHistoryRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_B3;
    }

    @Override
    public SendArticleEventHistory as(String alias) {
        return new SendArticleEventHistory(DSL.name(alias), this);
    }

    @Override
    public SendArticleEventHistory as(Name alias) {
        return new SendArticleEventHistory(alias, this);
    }

    @Override
    public SendArticleEventHistory as(Table<?> alias) {
        return new SendArticleEventHistory(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public SendArticleEventHistory rename(String name) {
        return new SendArticleEventHistory(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SendArticleEventHistory rename(Name name) {
        return new SendArticleEventHistory(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public SendArticleEventHistory rename(Table<?> name) {
        return new SendArticleEventHistory(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SendArticleEventHistory where(Condition condition) {
        return new SendArticleEventHistory(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SendArticleEventHistory where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SendArticleEventHistory where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SendArticleEventHistory where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SendArticleEventHistory where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SendArticleEventHistory where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SendArticleEventHistory where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SendArticleEventHistory where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SendArticleEventHistory whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SendArticleEventHistory whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
