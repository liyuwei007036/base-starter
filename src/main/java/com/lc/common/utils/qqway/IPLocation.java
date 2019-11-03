package com.lc.common.utils.qqway;

/**
 * @author l5990
 */
public class IPLocation {
    private String country;   
    private String area;   
       
    public IPLocation() {   
        country = area = "";   
    }   
       
    public IPLocation getCopy() {
        IPLocation ret = new IPLocation();
        ret.country = country;   
        ret.area = area;   
        return ret;   
    }   
  
    public String getCountry() {   
        return country;   
    }   
  
    public void setCountry(String country) {   
        this.country = country;   
    }   
  
    public String getArea() {   
        return area;   
    }   
  
    public void setArea(String area) {   
        if("CNET".equals(area.trim())){
            this.area="本机或本网络";   
        }else{   
            this.area = area;   
        }   
    }

	@Override
	public String toString() {
		return "" + area + "," + country;
	}
    
}  