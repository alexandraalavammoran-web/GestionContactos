//Es la clase base u objeto plano (POJO). 
//Define la estructura del dato de un contacto(sus atributos nombre, teléfono, email, categoría y si es favorito)
//Todo esto lo hace junto con sus métodos Getters y Setters.
package Model;

/**
 *
 * @author CRISTINA
 */
public class Persona {
    private String nombre;
    private String edad;
    private String telefono;
    private String cedula;
    private String email;
    private String pais;
    private String categoria;
    private boolean favorito;
    //Este es un constructor vacio por si acaso 
    public Persona(){
    } //Este es el constructor 
        public Persona(String nombre, String edad, String telefono, String cedula, String email, String pais, String categorio, boolean favorito){
            this.nombre = nombre;
            this.edad = edad;
            this.telefono = telefono;
            this.cedula = cedula;
            this.email = email;
            this.pais = pais;
            this.categoria = categorio;
            this.favorito = favorito;
        }
        // Son los métodos GETTER Y SETTER
       public String getNombre() {return nombre;}
       public void setNombre(String nombre){this.nombre = nombre;}
       
       public String getEdad() {return edad;}
       public void setEdad(String edad){this.edad = edad;}
       
       public String getTelefono() {return telefono;}
       public void setTelefono(String telefono){this.telefono = telefono;}
       
       public String getCedula() {return cedula;}
       public void setCedula(String cedula){this.cedula = cedula;}
       
       public String getEmail() {return email;}
       public void setEmail(String email){this.email = email;}
       
       public String getPais() {return pais;}
       public void setPais(String pais){this.pais = pais;}
       
       public String getCategoria() {return categoria;}
       public void setCategoria(String categoria){this.categoria = categoria;}
       
       public boolean isFavorito(){return favorito;}
       public void setFavorito(boolean favorito){this.favorito = favorito;}
    }

