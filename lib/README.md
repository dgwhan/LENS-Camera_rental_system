# Project Libraries & Dependencies

Thư mục này dùng để chứa các thư viện `.jar` cần thiết của dự án (Jakarta EE APIs, Database Drivers, v.v.) để bất kỳ ai clone project về máy cũng có thể mở và chạy được ngay mà không phụ thuộc vào đường dẫn GlassFish cục bộ của từng máy.

---

### Các file `.jar` cần đặt vào thư mục này:

1. **EJB & Persistence:**
   * `jakarta.ejb-api.jar` (chứa `@Stateless`, `@EJB`, `@Local`,...)
   * `jakarta.persistence-api.jar` (chứa `@Entity`, `@PersistenceContext`, `EntityManager`,...)
   * `jakarta.transaction-api.jar`

2. **JSF & CDI:**
   * `jakarta.faces.jar` (chứa `@Named`, `FacesContext`, `@ViewScoped`,...)
   * `jakarta.enterprise.cdi-api.jar`
   * `jakarta.inject-api.jar`
   * `jakarta.annotation-api.jar`

3. **Servlet & Validation:**
   * `jakarta.servlet-api.jar`
   * `jakarta.validation-api.jar`

4. **Security & Authentication (Jakarta Security API):**
   * `jakarta.security.enterprise-api.jar` (chứa `SecurityContext`, `IdentityStore`, `CallerPrincipal`,...)
   * `jakarta.authentication-api.jar`
   * `jakarta.authorization-api.jar`

5. **Database Driver (nếu có kết nối trực tiếp):**
   * `mssql-jdbc.jar`

---

### Nguồn lấy nhanh các file `.jar`:
Các file trên có sẵn trong thư mục GlassFish của bạn tại:
`<GlassFish_Installation_Directory>/glassfish/modules/`
(Ví dụ: `C:\Users\<User>\GlassFish_Server_800\glassfish\modules\`)
