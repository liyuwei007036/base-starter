package live.lumia.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * @author l5990
 */
@Data
public class Account implements Serializable {

    private String id;

    private String account;

    private String name;

    private String roleName;

    private List<String> powers;

    private Boolean hasAllPowers;
}
