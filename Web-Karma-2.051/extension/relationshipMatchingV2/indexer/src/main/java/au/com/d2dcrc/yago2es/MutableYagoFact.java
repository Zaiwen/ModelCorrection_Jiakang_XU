package au.com.d2dcrc.yago2es;

/**
 * Represents a single fact from the YAGO knowledge base.
 * <p>
 *     Mutable, so that instances can be reused for for efficiency when processing a large number of facts.
 * </p>
 */
public final class MutableYagoFact implements Cloneable{

    /**
     * The URI of the namespace of the fact id.
     * <p>May be null if the fact id is a URI.</p>
     */
    public CharSequence factNamespace;

    /**
     * The prefix used when shortening the qualified fact id.
     * <p>May be null if the fact id is a URI.</p>
     */
    public CharSequence factPrefix;

    /**
     * The unqualified (no namespace, no prefix) fact id.
     * <p>May be a URI, in which case {@link #factNamespace} and {@link #factPrefix} will be null.</p>
     */
    public CharSequence factId;

    /**
     * The URI of the namespace of the subject.
     * <p>May be null if the subject is a URI.</p>
     */
    public CharSequence subjectNamespace;

    /**
     * The prefix used when shortening the qualified subject.
     * <p>May be null if the subject is a URI.</p>
     */
    public CharSequence subjectPrefix;

    /**
     * The unqualified (no namespace, no prefix) subject.
     * <p>May be a URI, in which case {@link #subjectNamespace} and {@link #subjectPrefix} will be null.</p>
     */
    public CharSequence subject;

    /**
     * The URI of the namespace of the predicate.
     * <p>May be null if the predicate is a URI.</p>
     */
    public CharSequence predicateNamespace;

    /**
     * The prefix used when shortening the qualified predicate.
     * <p>May be null if the predicate is a URI.</p>
     */
    public CharSequence predicatePrefix;

    /**
     * The unqualified (no namespace, no prefix) predicate.
     * <p>May be a URI, in which case {@link #predicateNamespace} and {@link #predicatePrefix} will be null.</p>
     */
    public CharSequence predicate;

    /**
     * The URI of the namespace of the object.
     * <p>May be null if the object is a URI.</p>
     */
    public CharSequence objectNamespace;

    /**
     * The prefix used when shortening the qualified object.
     * <p>May be null if the object is a URI.</p>
     */
    public CharSequence objectPrefix;

    /**
     * The unqualified (no namespace, no prefix) object.
     * <p>May be a URI, in which case {@link #objectNamespace} and {@link #objectPrefix} will be null.</p>
     */
    public CharSequence object;

    /**
     * The URI of the type of the value.
     * <p>May be null if the type is a URI.</p>
     */
    public CharSequence valueTypeNamespace;

    /**
     * The prefix used when shortening the qualified type of the value.
     * <p>May be null if the type of the value is a URI.</p>
     */
    public CharSequence valueTypePrefix;

    /**
     * The unqualified (no namespace, no prefix) type of the value.
     * <p>May be a URI, in qhich case {@link #valueTypeNamespace} and {@link #valueTypePrefix} will be null.</p>
     */
    public CharSequence valueType;

    /**
     * The language code for the value.
     * <p>May be null if no specific language is associated with the value.</p>
     */
    public CharSequence valueLanguage;

    /**
     * The value as a string.
     * <p>The type of the value determines how this string should be parsed.</p>
     */
    public CharSequence value;

    /**
     * Sets all of the fields of the fact to {@code null}.
     */
    public void clear(){
        factNamespace = null;
        factPrefix = null;
        factId = null;
        subjectNamespace = null;
        subjectPrefix = null;
        subject = null;
        predicateNamespace = null;
        predicatePrefix = null;
        predicate = null;
        objectNamespace = null;
        objectPrefix = null;
        object = null;
        valueTypeNamespace = null;
        valueTypePrefix = null;
        valueType = null;
        valueLanguage = null;
        value = null;
    }

    /**
     * Creates a copy of the fact.
     * <p>
     *     Particularly useful when caching facts to ensure that other functions do not mutate them.
     * </p>
     * 
     * @return A copy of the fact.
     */
    public MutableYagoFact clone(){
        final MutableYagoFact newFact = new MutableYagoFact();
        newFact.factNamespace = factNamespace;
        newFact.factPrefix = factPrefix;
        newFact.factId = factId;
        newFact.subjectNamespace = subjectNamespace;
        newFact.subjectPrefix = subjectPrefix;
        newFact.subject = subject;
        newFact.predicateNamespace = predicateNamespace;
        newFact.predicatePrefix = predicatePrefix;
        newFact.predicate = predicate;
        newFact.objectNamespace = objectNamespace;
        newFact.objectPrefix = objectPrefix;
        newFact.object = object;
        newFact.valueTypeNamespace = valueTypeNamespace;
        newFact.valueTypePrefix = valueTypePrefix;
        newFact.valueType = valueType;
        newFact.valueLanguage = valueLanguage;
        newFact.value = value;
        return newFact;
    }
}
