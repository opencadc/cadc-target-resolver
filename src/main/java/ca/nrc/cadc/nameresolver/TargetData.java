/*
 ************************************************************************
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 *
 * (c) 2007.                            (c) 2007.
 * National Research Council            Conseil national de recherches
 * Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 * All rights reserved                  Tous droits reserves
 *
 * NRC disclaims any warranties         Le CNRC denie toute garantie
 * expressed, implied, or statu-        enoncee, implicite ou legale,
 * tory, of any kind with respect       de quelque nature que se soit,
 * to the software, including           concernant le logiciel, y com-
 * without limitation any war-          pris sans restriction toute
 * ranty of merchantability or          garantie de valeur marchande
 * fitness for a particular pur-        ou de pertinence pour un usage
 * pose.  NRC shall not be liable       particulier.  Le CNRC ne
 * in any event for any damages,        pourra en aucun cas etre tenu
 * whether direct or indirect,          responsable de tout dommage,
 * special or general, consequen-       direct ou indirect, particul-
 * tial or incidental, arising          ier ou general, accessoire ou
 * from the use of the software.        fortuit, resultant de l'utili-
 *                                      sation du logiciel.
 *
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 ************************************************************************
 */

package ca.nrc.cadc.nameresolver;

import ca.nrc.cadc.util.StringUtil;

/**
 * @author jburke
 */
public class TargetData {
    // Set this if need be.
    private final String errorMessage;

    private final String target;
    private final String host;
    private final String database;
    private final double ra;
    private final double dec;
    private final String coordsys;
    private final String objectName;
    private final String objectType;
    private final String morphologyType;
    private final long timestamp;

    private long queryTime;
    private boolean cached;


    public TargetData(final String errorMessage) {
        this(null, null, null, 0.0d, 0.0d, null, null,
             null, errorMessage);
    }

    /**
     * Constructs a new TargetData initialized with the specified target name,
     * host name, resolver name, ra, dec, object name, object type, and
     * morphology type. The coordinate system will  currently always be ICRS.
     *
     * @param target         the target name.
     * @param host           the host name.
     * @param database       the database name.
     * @param ra             the Right Ascension in degrees.
     * @param dec            the Declination in degrees.
     * @param objectName     the preferred object name.
     * @param objecType      the object type.
     * @param morphologyType the morphology type.
     * @param errorMessage   Error message.
     */
    public TargetData(final String target, final String host, final String database, final double ra, final double dec,
                      final String objectName, final String objecType, final String morphologyType,
                      final String errorMessage) {
        this.target = target;
        this.host = host;
        this.database = database;
        this.ra = ra;
        this.dec = dec;
        this.coordsys = "ICRS";
        this.objectName = objectName;
        this.objectType = objecType;
        this.morphologyType = morphologyType;
        this.cached = false;
        this.queryTime = 0;
        this.timestamp = System.currentTimeMillis();
        this.errorMessage = errorMessage;
    }

    /**
     * Constructs a new TargetData initialized with the specified target name,
     * host name, resolver name, ra, dec, object name, object type, and
     * morphology type. The coordinate system will  currently always be ICRS.
     *
     * @param target         the target name.
     * @param host           the host name.
     * @param database       the database name.
     * @param ra             the Right Ascension in degrees.
     * @param dec            the Declination in degrees.
     * @param objectName     the preferred object name.
     * @param objectType     the object type.
     * @param morphologyType the morphology type.
     */
    public TargetData(final String target, final String host, final String database, final double ra, final double dec,
                      final String objectName, final String objectType, final String morphologyType) {
        this(target, host, database, ra, dec, objectName, objectType, morphologyType, null);
    }

    /**
     * @return String value of the coordinate system.
     */
    public String getCoordsys() {
        return coordsys;
    }

    /**
     * @return String value of the database name.
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @return double value of the declination in degrees.
     */
    public double getDEC() {
        return dec;
    }

    /**
     * @return String value of the host name.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return double value of the right ascension in degrees.
     */
    public double getRA() {
        return ra;
    }

    /**
     * @return String value of the target name.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return String value of the preferred name for the object.
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @return String value describing the object type.
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * @return String value of the object morphology.
     */
    public String getMorphologyType() {
        return morphologyType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return StringUtil.hasText(errorMessage);
    }

    /**
     * @return long value of elapsed query time.
     */
    public long getQueryTime() {
        return queryTime;
    }

    /**
     * @return long, object instantiation timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param cached whether data is from the cache.
     */
    public void setCached(boolean cached) {
        this.cached = cached;
    }

    /**
     * @param time elapsed query time.
     */
    public void setQueryTime(long time) {
        this.queryTime = time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("host=").append(host);
        sb.append(",database=").append(database);
        sb.append(",ra=").append(ra);
        sb.append(",dec=").append(dec);
        sb.append(",cached=").append(cached);
        if (objectName != null) {
            sb.append(",oname=").append(objectName);
        }
        if (objectType != null) {
            sb.append(",otype=").append(objectType);
        }
        if (morphologyType != null) {
            sb.append(",mtype=").append(morphologyType);
        }
        sb.append(",time=").append(queryTime);
        return sb.toString();
    }

}
