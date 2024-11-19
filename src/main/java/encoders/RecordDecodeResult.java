package encoders;

public class RecordDecodeResult {
	
	private String field_name;
	private Object value;
	private int[] encoded;

	public String getField_name() {
		return field_name;
	}

	public void setField_name(String field_name) {
		this.field_name = field_name;
	}

	public int[] getEncoded() {
		return encoded;
	}

	public void setEncoded(int[] encoded) {
		this.encoded = encoded;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object object) {
		this.value = object;
	}
	
	@Override
	public String toString() {
		
		String next = field_name + "\n";
		next += value.toString() + "\n";
		
		for(int i = 0; i < encoded.length; i++) {
			next += encoded[i] + " ";
		}
		next += "\n";
		
		return next;
	}
	
}
