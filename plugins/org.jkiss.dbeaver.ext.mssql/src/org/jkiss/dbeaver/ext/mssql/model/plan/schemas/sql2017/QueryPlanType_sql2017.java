
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2017;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			New Runtime information:
 * 			DegreeOfParallelism
 * 			EffectiveDegreeOfParallelism: Max parallelism used by columnstore index build
 * 			MemoryGrant (in kilobytes)
 * 			
 * 			New compile time information:
 * 			mem fractions
 * 			CachedPlanSize (in kilobytes)
 * 			CompileTime (in milliseconds)
 * 			CompileCPU (in milliseconds)
 * 			CompileMemory (in kilobytes)
 * 			Parameter values used during query compilation
 * 			NonParallelPlanReason
 * 			
 * 
 * <p>Java class for QueryPlanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryPlanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InternalInfo" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}InternalInfoType" minOccurs="0"/>
 *         &lt;element name="ThreadStat" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}ThreadStatType" minOccurs="0"/>
 *         &lt;element name="MissingIndexes" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}MissingIndexesType" minOccurs="0"/>
 *         &lt;element name="GuessedSelectivity" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}GuessedSelectivityType" minOccurs="0"/>
 *         &lt;element name="UnmatchedIndexes" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}UnmatchedIndexesType" minOccurs="0"/>
 *         &lt;element name="Warnings" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}WarningsType" minOccurs="0"/>
 *         &lt;element name="MemoryGrantInfo" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}MemoryGrantType" minOccurs="0"/>
 *         &lt;element name="OptimizerHardwareDependentProperties" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}OptimizerHardwareDependentPropertiesType" minOccurs="0"/>
 *         &lt;element name="OptimizerStatsUsage" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}OptimizerStatsUsageType" minOccurs="0"/>
 *         &lt;element name="TraceFlags" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}TraceFlagListType" maxOccurs="2" minOccurs="0"/>
 *         &lt;element name="WaitStats" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}WaitStatListType" minOccurs="0"/>
 *         &lt;element name="QueryTimeStats" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}QueryExecTimeType" minOccurs="0"/>
 *         &lt;element name="RelOp" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}RelOpType"/>
 *         &lt;element name="ParameterList" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}ColumnReferenceListType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="DegreeOfParallelism" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="EffectiveDegreeOfParallelism" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="NonParallelPlanReason" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="MemoryGrant" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *       &lt;attribute name="CachedPlanSize" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *       &lt;attribute name="CompileTime" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *       &lt;attribute name="CompileCPU" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *       &lt;attribute name="CompileMemory" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *       &lt;attribute name="UsePlan" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="ContainsInterleavedExecutionCandidates" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryPlanType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "internalInfo",
    "threadStat",
    "missingIndexes",
    "guessedSelectivity",
    "unmatchedIndexes",
    "warnings",
    "memoryGrantInfo",
    "optimizerHardwareDependentProperties",
    "optimizerStatsUsage",
    "traceFlags",
    "waitStats",
    "queryTimeStats",
    "relOp",
    "parameterList"
})
public class QueryPlanType_sql2017 {

    @XmlElement(name = "InternalInfo", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected InternalInfoType_sql2017 internalInfo;
    @XmlElement(name = "ThreadStat", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected ThreadStatType_sql2017 threadStat;
    @XmlElement(name = "MissingIndexes", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected MissingIndexesType_sql2017 missingIndexes;
    @XmlElement(name = "GuessedSelectivity", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected GuessedSelectivityType_sql2017 guessedSelectivity;
    @XmlElement(name = "UnmatchedIndexes", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected UnmatchedIndexesType_sql2017 unmatchedIndexes;
    @XmlElement(name = "Warnings", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected WarningsType_sql2017 warnings;
    @XmlElement(name = "MemoryGrantInfo", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected MemoryGrantType_sql2017 memoryGrantInfo;
    @XmlElement(name = "OptimizerHardwareDependentProperties", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected OptimizerHardwareDependentPropertiesType_sql2017 optimizerHardwareDependentProperties;
    @XmlElement(name = "OptimizerStatsUsage", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected OptimizerStatsUsageType_sql2017 optimizerStatsUsage;
    @XmlElement(name = "TraceFlags", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected List<TraceFlagListType_sql2017> traceFlags;
    @XmlElement(name = "WaitStats", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected WaitStatListType_sql2017 waitStats;
    @XmlElement(name = "QueryTimeStats", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected QueryExecTimeType_sql2017 queryTimeStats;
    @XmlElement(name = "RelOp", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected RelOpType_sql2017 relOp;
    @XmlElement(name = "ParameterList", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected ColumnReferenceListType_sql2017 parameterList;
    @XmlAttribute(name = "DegreeOfParallelism")
    protected Integer degreeOfParallelism;
    @XmlAttribute(name = "EffectiveDegreeOfParallelism")
    protected Integer effectiveDegreeOfParallelism;
    @XmlAttribute(name = "NonParallelPlanReason")
    protected String nonParallelPlanReason;
    @XmlAttribute(name = "MemoryGrant")
    @XmlSchemaType(name = "unsignedLong")
    protected BigInteger memoryGrant;
    @XmlAttribute(name = "CachedPlanSize")
    @XmlSchemaType(name = "unsignedLong")
    protected BigInteger cachedPlanSize;
    @XmlAttribute(name = "CompileTime")
    @XmlSchemaType(name = "unsignedLong")
    protected BigInteger compileTime;
    @XmlAttribute(name = "CompileCPU")
    @XmlSchemaType(name = "unsignedLong")
    protected BigInteger compileCPU;
    @XmlAttribute(name = "CompileMemory")
    @XmlSchemaType(name = "unsignedLong")
    protected BigInteger compileMemory;
    @XmlAttribute(name = "UsePlan")
    protected Boolean usePlan;
    @XmlAttribute(name = "ContainsInterleavedExecutionCandidates")
    protected Boolean containsInterleavedExecutionCandidates;

    /**
     * Gets the value of the internalInfo property.
     * 
     * @return
     *     possible object is
     *     {@link InternalInfoType_sql2017 }
     *     
     */
    public InternalInfoType_sql2017 getInternalInfo() {
        return internalInfo;
    }

    /**
     * Sets the value of the internalInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link InternalInfoType_sql2017 }
     *     
     */
    public void setInternalInfo(InternalInfoType_sql2017 value) {
        this.internalInfo = value;
    }

    /**
     * Gets the value of the threadStat property.
     * 
     * @return
     *     possible object is
     *     {@link ThreadStatType_sql2017 }
     *     
     */
    public ThreadStatType_sql2017 getThreadStat() {
        return threadStat;
    }

    /**
     * Sets the value of the threadStat property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreadStatType_sql2017 }
     *     
     */
    public void setThreadStat(ThreadStatType_sql2017 value) {
        this.threadStat = value;
    }

    /**
     * Gets the value of the missingIndexes property.
     * 
     * @return
     *     possible object is
     *     {@link MissingIndexesType_sql2017 }
     *     
     */
    public MissingIndexesType_sql2017 getMissingIndexes() {
        return missingIndexes;
    }

    /**
     * Sets the value of the missingIndexes property.
     * 
     * @param value
     *     allowed object is
     *     {@link MissingIndexesType_sql2017 }
     *     
     */
    public void setMissingIndexes(MissingIndexesType_sql2017 value) {
        this.missingIndexes = value;
    }

    /**
     * Gets the value of the guessedSelectivity property.
     * 
     * @return
     *     possible object is
     *     {@link GuessedSelectivityType_sql2017 }
     *     
     */
    public GuessedSelectivityType_sql2017 getGuessedSelectivity() {
        return guessedSelectivity;
    }

    /**
     * Sets the value of the guessedSelectivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link GuessedSelectivityType_sql2017 }
     *     
     */
    public void setGuessedSelectivity(GuessedSelectivityType_sql2017 value) {
        this.guessedSelectivity = value;
    }

    /**
     * Gets the value of the unmatchedIndexes property.
     * 
     * @return
     *     possible object is
     *     {@link UnmatchedIndexesType_sql2017 }
     *     
     */
    public UnmatchedIndexesType_sql2017 getUnmatchedIndexes() {
        return unmatchedIndexes;
    }

    /**
     * Sets the value of the unmatchedIndexes property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnmatchedIndexesType_sql2017 }
     *     
     */
    public void setUnmatchedIndexes(UnmatchedIndexesType_sql2017 value) {
        this.unmatchedIndexes = value;
    }

    /**
     * Gets the value of the warnings property.
     * 
     * @return
     *     possible object is
     *     {@link WarningsType_sql2017 }
     *     
     */
    public WarningsType_sql2017 getWarnings() {
        return warnings;
    }

    /**
     * Sets the value of the warnings property.
     * 
     * @param value
     *     allowed object is
     *     {@link WarningsType_sql2017 }
     *     
     */
    public void setWarnings(WarningsType_sql2017 value) {
        this.warnings = value;
    }

    /**
     * Gets the value of the memoryGrantInfo property.
     * 
     * @return
     *     possible object is
     *     {@link MemoryGrantType_sql2017 }
     *     
     */
    public MemoryGrantType_sql2017 getMemoryGrantInfo() {
        return memoryGrantInfo;
    }

    /**
     * Sets the value of the memoryGrantInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link MemoryGrantType_sql2017 }
     *     
     */
    public void setMemoryGrantInfo(MemoryGrantType_sql2017 value) {
        this.memoryGrantInfo = value;
    }

    /**
     * Gets the value of the optimizerHardwareDependentProperties property.
     * 
     * @return
     *     possible object is
     *     {@link OptimizerHardwareDependentPropertiesType_sql2017 }
     *     
     */
    public OptimizerHardwareDependentPropertiesType_sql2017 getOptimizerHardwareDependentProperties() {
        return optimizerHardwareDependentProperties;
    }

    /**
     * Sets the value of the optimizerHardwareDependentProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link OptimizerHardwareDependentPropertiesType_sql2017 }
     *     
     */
    public void setOptimizerHardwareDependentProperties(OptimizerHardwareDependentPropertiesType_sql2017 value) {
        this.optimizerHardwareDependentProperties = value;
    }

    /**
     * Gets the value of the optimizerStatsUsage property.
     * 
     * @return
     *     possible object is
     *     {@link OptimizerStatsUsageType_sql2017 }
     *     
     */
    public OptimizerStatsUsageType_sql2017 getOptimizerStatsUsage() {
        return optimizerStatsUsage;
    }

    /**
     * Sets the value of the optimizerStatsUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link OptimizerStatsUsageType_sql2017 }
     *     
     */
    public void setOptimizerStatsUsage(OptimizerStatsUsageType_sql2017 value) {
        this.optimizerStatsUsage = value;
    }

    /**
     * Gets the value of the traceFlags property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the traceFlags property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTraceFlags().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TraceFlagListType_sql2017 }
     * 
     * 
     */
    public List<TraceFlagListType_sql2017> getTraceFlags() {
        if (traceFlags == null) {
            traceFlags = new ArrayList<TraceFlagListType_sql2017>();
        }
        return this.traceFlags;
    }

    /**
     * Gets the value of the waitStats property.
     * 
     * @return
     *     possible object is
     *     {@link WaitStatListType_sql2017 }
     *     
     */
    public WaitStatListType_sql2017 getWaitStats() {
        return waitStats;
    }

    /**
     * Sets the value of the waitStats property.
     * 
     * @param value
     *     allowed object is
     *     {@link WaitStatListType_sql2017 }
     *     
     */
    public void setWaitStats(WaitStatListType_sql2017 value) {
        this.waitStats = value;
    }

    /**
     * Gets the value of the queryTimeStats property.
     * 
     * @return
     *     possible object is
     *     {@link QueryExecTimeType_sql2017 }
     *     
     */
    public QueryExecTimeType_sql2017 getQueryTimeStats() {
        return queryTimeStats;
    }

    /**
     * Sets the value of the queryTimeStats property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryExecTimeType_sql2017 }
     *     
     */
    public void setQueryTimeStats(QueryExecTimeType_sql2017 value) {
        this.queryTimeStats = value;
    }

    /**
     * Gets the value of the relOp property.
     * 
     * @return
     *     possible object is
     *     {@link RelOpType_sql2017 }
     *     
     */
    public RelOpType_sql2017 getRelOp() {
        return relOp;
    }

    /**
     * Sets the value of the relOp property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelOpType_sql2017 }
     *     
     */
    public void setRelOp(RelOpType_sql2017 value) {
        this.relOp = value;
    }

    /**
     * Gets the value of the parameterList property.
     * 
     * @return
     *     possible object is
     *     {@link ColumnReferenceListType_sql2017 }
     *     
     */
    public ColumnReferenceListType_sql2017 getParameterList() {
        return parameterList;
    }

    /**
     * Sets the value of the parameterList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColumnReferenceListType_sql2017 }
     *     
     */
    public void setParameterList(ColumnReferenceListType_sql2017 value) {
        this.parameterList = value;
    }

    /**
     * Gets the value of the degreeOfParallelism property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDegreeOfParallelism() {
        return degreeOfParallelism;
    }

    /**
     * Sets the value of the degreeOfParallelism property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDegreeOfParallelism(Integer value) {
        this.degreeOfParallelism = value;
    }

    /**
     * Gets the value of the effectiveDegreeOfParallelism property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEffectiveDegreeOfParallelism() {
        return effectiveDegreeOfParallelism;
    }

    /**
     * Sets the value of the effectiveDegreeOfParallelism property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEffectiveDegreeOfParallelism(Integer value) {
        this.effectiveDegreeOfParallelism = value;
    }

    /**
     * Gets the value of the nonParallelPlanReason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNonParallelPlanReason() {
        return nonParallelPlanReason;
    }

    /**
     * Sets the value of the nonParallelPlanReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNonParallelPlanReason(String value) {
        this.nonParallelPlanReason = value;
    }

    /**
     * Gets the value of the memoryGrant property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMemoryGrant() {
        return memoryGrant;
    }

    /**
     * Sets the value of the memoryGrant property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMemoryGrant(BigInteger value) {
        this.memoryGrant = value;
    }

    /**
     * Gets the value of the cachedPlanSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCachedPlanSize() {
        return cachedPlanSize;
    }

    /**
     * Sets the value of the cachedPlanSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCachedPlanSize(BigInteger value) {
        this.cachedPlanSize = value;
    }

    /**
     * Gets the value of the compileTime property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCompileTime() {
        return compileTime;
    }

    /**
     * Sets the value of the compileTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCompileTime(BigInteger value) {
        this.compileTime = value;
    }

    /**
     * Gets the value of the compileCPU property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCompileCPU() {
        return compileCPU;
    }

    /**
     * Sets the value of the compileCPU property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCompileCPU(BigInteger value) {
        this.compileCPU = value;
    }

    /**
     * Gets the value of the compileMemory property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCompileMemory() {
        return compileMemory;
    }

    /**
     * Sets the value of the compileMemory property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCompileMemory(BigInteger value) {
        this.compileMemory = value;
    }

    /**
     * Gets the value of the usePlan property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getUsePlan() {
        return usePlan;
    }

    /**
     * Sets the value of the usePlan property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUsePlan(Boolean value) {
        this.usePlan = value;
    }

    /**
     * Gets the value of the containsInterleavedExecutionCandidates property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getContainsInterleavedExecutionCandidates() {
        return containsInterleavedExecutionCandidates;
    }

    /**
     * Sets the value of the containsInterleavedExecutionCandidates property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsInterleavedExecutionCandidates(Boolean value) {
        this.containsInterleavedExecutionCandidates = value;
    }

}
