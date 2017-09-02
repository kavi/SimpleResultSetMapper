package dk.javacode.srsm.testmodel;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;
import dk.javacode.srsm.converters.BooleanIntJdbcConverter;

@Table(name = "address")
public class Address {

	@Column(primaryKey = true, name = "id")
	private Long id2;

	@Column(name = "street_name")
	private String streetName;

	@Column(name = "is_public", dataConverter = BooleanIntJdbcConverter.class)
	private Boolean isPublic;

	public Long getId2() {
		return id2;
	}

	public void setId2(Long id) {
		this.id2 = id;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	@Override
	public String toString() {
		return "Address [id=" + id2 + ", streetName=" + streetName + ", isPublic=" + isPublic + "]";
	}

}
