package com.wendesday.bippobippo;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the BippoBippo content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. A well-written client depends only on the constants in the contract.
 */
public final class BippoBippo {
    public static final String AUTHORITY = "com.wendesday.bippobippo";
    /**
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";
    
    // This class cannot be instantiated
    private BippoBippo() {
    }
    
    /**
     * Buddy table contract
     */
    public static final class Person implements BaseColumns {

        // This class cannot be instantiated
        private Person() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "person";

        /*
         * URI definitions
         */

        /**
         * Path parts for the buddy URIs
         */
        private static final String PATH_PERSON = "/person";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_PERSON);

   
        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of buddy.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/bippobippo.person";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * buddy.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/bippobippo.person";


        /*
         * Column definitions
         */

        public static final String DISPLAY_NAME = "displayname";
        public static final String PHONE_NUMBER = "phone_number";

        public static final String BIRTHDAY = "birthday";
        
    }

    /**
     * Buddy table contract
     */
    public static final class SensorData implements BaseColumns {

        // This class cannot be instantiated
        private SensorData() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "sensordata";

        /*
         * URI definitions
         */

        /**
         * Path parts for the buddy URIs
         */
        private static final String PATH_SENSOR_DATA = "/sensordata";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_SENSOR_DATA);

   
        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of buddy.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/bippobippo.sensordata";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * buddy.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/bippobippo.sensordata";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "timestamp ASC";

        /*
         * Column definitions
         */

        public static final String HEAT = "heat";
        
        public static final String WET = "wet";
        
        public static final String BPM = "bpm";
        
        public static final String MIC = "mic";

        public static final String TIMESTAMP = "timestamp";
        

       
    }
    
}