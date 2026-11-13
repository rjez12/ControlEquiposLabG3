# Control de equipos de laboratorio

Aplicación de escritorio para administrar el catálogo de equipos de un laboratorio universitario con JavaFX, Hibernate ORM y PostgreSQL.

Permite registrar, consultar, actualizar y eliminar equipos, buscar por una parte del nombre y filtrar por tipo. No incluye préstamos, usuarios, autenticación ni reportes.

## Tecnologías y requisitos

- JDK 21.
- JavaFX 21.0.6.
- Hibernate ORM 6.6.5.Final y Jakarta Persistence 3.1.
- PostgreSQL y driver JDBC 42.7.5.
- Maven, disponible mediante el wrapper del proyecto.
- IntelliJ IDEA y pgAdmin para la configuración local.

## Organización

| Ubicación | Responsabilidad |
| --- | --- |
| `src/main/java/com/desarrollo/controlequiposlabg3/model` | Entidades `Equipo` y `TipoEquipo`. |
| `src/main/java/com/desarrollo/controlequiposlabg3/dao` | Consultas, CRUD y transacciones. |
| `src/main/java/com/desarrollo/controlequiposlabg3/database` | Administración de `EntityManagerFactory` con `JPAUtil`. |
| `src/main/java/com/desarrollo/controlequiposlabg3/controller` | Eventos, validaciones y presentación. |
| `src/main/resources/com/desarrollo/controlequiposlabg3` | Vista FXML y estilos CSS. |
| `src/main/resources/META-INF/persistence.xml` | Unidad de persistencia y conexión. |
| `sql/bd_laboratorio_orm.sql` | Creación de base, esquema, tablas, restricciones y datos iniciales. |
| `docs/matriz-pruebas.md` | Casos P1–P8, resultados y evidencias. |
| `docs/evidencias` | Capturas de las pruebas realizadas. |

## Preparar PostgreSQL

La estructura se crea manualmente con SQL. El script debe ejecutarse por secciones en pgAdmin:

1. Abrir el Query Tool de `postgres` y ejecutar únicamente `CREATE DATABASE bd_laboratorio_orm;`. Esta instrucción se ejecuta fuera de una transacción. Si la base ya existe, omitirla.
2. Abrir un nuevo Query Tool desde `bd_laboratorio_orm`.
3. Ejecutar la sección restante del script: esquema, tablas, restricciones, registros iniciales y verificaciones.

El script está destinado a una instalación nueva; no volver a ejecutar las instrucciones de creación sobre tablas ya existentes.

La base inicial contiene tres tipos y ocho equipos. El esquema se llama `inventario`.

## Configuración local

La unidad de persistencia se llama **`laboratorioPU`**. En `persistence.xml`, la conexión predeterminada es:

```text
URL: jdbc:postgresql://localhost:5432/bd_laboratorio_orm
Usuario: postgres
```

Modificar el usuario, servidor o puerto si el entorno local utiliza otros valores.

La contraseña se obtiene con `System.getenv("BD_PASSWORD")` en `JPAUtil`; no se escribe en el código ni en el repositorio.

`hibernate.hbm2ddl.auto=validate` verifica la correspondencia entre las entidades y las tablas existentes. Hibernate no crea ni modifica el esquema.

## Ejecutar en IntelliJ IDEA

1. Abrir el proyecto y recargar las dependencias de Maven.
2. Seleccionar JDK 21 como SDK del proyecto.
3. Crear una configuración **Application** para `com.desarrollo.controlequiposlabg3.LaboratorioApplication`.
4. Seleccionar Java 21 y la raíz del proyecto como directorio de trabajo.
5. En **Environment variables**, agregar `BD_PASSWORD` con la contraseña del usuario local de PostgreSQL.
6. Mantener desmarcada la opción **Store as project file** para esa configuración con credenciales.
7. Presionar **Run**.

La contraseña debe configurarse en la misma configuración de ejecución que se inicia. `JPAUtil` rechazará una variable ausente o vacía.

También se puede ejecutar desde PowerShell, en la raíz del proyecto:

```powershell
# Sustituir el valor de ejemplo únicamente en el entorno local.
$env:BD_PASSWORD = 'REEMPLAZAR_POR_LA_CLAVE_LOCAL'
.\mvnw.cmd clean javafx:run
```

## Entidades y asociación

- `TipoEquipo` representa `inventario.tipo_equipo`. Su atributo Java `id` se mapea a `id_tipo`.
- `Equipo` representa `inventario.equipo`. Su atributo Java `id` se mapea a `id_equipo`.
- `Equipo.tipoEquipo` utiliza `@ManyToOne` y `@JoinColumn(name = "id_tipo")`: cada equipo pertenece a un tipo existente.
- `TipoEquipo.equipos` utiliza `@OneToMany(mappedBy = "tipoEquipo")`: un tipo puede tener varios equipos.

No se configura eliminación en cascada. PostgreSQL rechaza eliminar un tipo que tenga equipos asociados mediante `ON DELETE RESTRICT`. La interfaz administra equipos; no incluye un CRUD de tipos.

## Persistencia y consultas

`guardar()` utiliza `persist()`. `modificar()` obtiene una entidad administrada con `find()` y cambia sus atributos; Hibernate detecta esos cambios al confirmar la transacción, por lo que no necesita `merge()`. `eliminar()` utiliza `find()` y `remove()`.

Cada escritura inicia una transacción con `begin()`, confirma con `commit()` y ejecuta `rollback()` si ocurre una excepción mientras la transacción permanece activa. Cada unidad de trabajo cierra su `EntityManager` en `finally`. La aplicación cierra `EntityManagerFactory` mediante `JPAUtil.cerrar()` al finalizar.

El listado y la búsqueda utilizan JPQL sobre entidades. `JOIN FETCH e.tipoEquipo` recupera el tipo junto con el equipo para poder mostrar su nombre después de cerrar el `EntityManager`.

La búsqueda usa `LOWER(e.nombre) LIKE :nombre`; el filtro agrega `e.tipoEquipo.id = :idTipo`. Los valores se suministran con `setParameter()`. El nombre y el tipo se pueden combinar. El CRUD no se implementa con JDBC directo.

## Uso

1. Completar código, nombre, tipo, estado y cantidad; la marca es opcional.
2. Presionar **Guardar equipo** para registrar un elemento nuevo.
3. Seleccionar una fila para cargar sus datos y habilitar **Actualizar** y **Eliminar equipo**.
4. Presionar **Limpiar** para volver al registro de un equipo nuevo.
5. Usar **Aplicar filtros** para consultar por nombre, por tipo o por ambos. **Restablecer** elimina los filtros.

Después de una escritura se mantienen los filtros activos. Un equipo que deje de cumplirlos puede no aparecer en la tabla; presionar **Restablecer** para consultar el inventario completo.

## Validaciones e integridad

Se rechazan código y nombre vacíos, cantidad no entera o negativa, ausencia de tipo y estados no permitidos. También se verifican las longitudes de código, nombre y marca.

Los estados permitidos son `Disponible`, `En mantenimiento` y `Fuera de servicio`.

PostgreSQL aplica claves primarias, código único, nombre de tipo único, campos obligatorios, clave foránea y restricciones CHECK. Un código duplicado provoca un error de integridad: el DAO gestiona la reversión y el controlador muestra un Alert sin anunciar éxito.

## Pruebas y evidencias

Consultar [la matriz P1–P8](docs/matriz-pruebas.md).}
Repositorio: https://github.com/rjez12/ControlEquiposLabG3
