package redis.clients.johm;

/**
 * NVPair provides a helper-class for providing the attribute name and values.
 * It allows queries for attributes
 */
public class NVPair {
        private String attributeName;
        private Object attributeValue;
        
        public NVPair(String attributeName, Object attributeValue) {
            this.attributeName=attributeName;
            this.attributeValue=attributeValue;
        }
        
        /**
         * Get the attribute name
         */
        public String getAttributeName() {
            return(attributeName);
        }
        
       /**
        * Get the attribute values
        */
        public Object getAttributeValue() {
            return(attributeValue);
        }
    }
