# Parcial
Parcial de primer corte, Arquitectura Empresarial.

## Descripción
Este proyecto implementa un almacenamiento llave-valor distribuido con una arquitectura de tres componentes:
1. **Cliente (HTML + JS)**: Interfaz para interactuar con el sistema.
2. **Fachada**: Valida y reenvía solicitudes al backend.
3. **Backend**: Almacena y recupera las tuplas llave-valor.

## API
### 1. GET /setkv?key={key}&value={value}
- **Descripción**: Crea o reemplaza el valor asociado a una llave.
- **Respuestas**:
  - `200 OK`: { "key": "mi_llave", "value": "mi_valor", "status": "created" }
  - `400 Bad Request`: { "error": "Invalid key or value" }

### 2. GET /getkv?key={key}
- **Descripción**: Obtiene el valor de una llave.
- **Respuestas**:
  - `200 OK`: { "key": "mi_llave", "value": "mi_valor" }
  - `404 Not Found`: { "error": "key_not_found", "key": "mi_llave" }


# Ejecucion
```
mvn clean compile
```
```
java -cp target/classes edu.eci.arep.backend.BackendServer
```
# Acceso + ejemplos de uso
```
http://localhost:35000/setkv?key=testKey&value=testValue
```

# Casos de uso
###  Crear o actualizar una tupla llave-valor
```
GET /setkv?key=nombre&value=Juan
```

### Obtener el valor de una llave existente
```
GET /getkv?key=nombre
```

### Obtener el valor de una llave inexistente
```
GET /getkv?key=apellido
```