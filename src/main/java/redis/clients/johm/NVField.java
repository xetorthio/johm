package redis.clients.johm;

/**
 * NVPair provides a helper-class for providing the attribute name and values.
 * It allows queries for attributes
 */
public class NVField {
        private String attributeName;
        private Object attributeValue;
        
        public NVField(String attributeName, Object attributeValue) {
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
