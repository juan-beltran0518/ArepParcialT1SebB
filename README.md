# Parcial
Parcial de primer corte, Arquitectura Empresarial.

## Descripción
Este Parcial implementa un almacenamiento llave-valor distribuido con una arquitectura de tres componentes:
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
1. mvn clean compile
```
```
2. java -cp target/classes edu.eci.arep.backend.BackendServer
```
# Acceso + ejemplos de uso
```
http://localhost:35000/setkv?key=testKey&value=testValue
```

# Casos de uso
1. Crear o actualizar una tupla llave-valor
```
GET /setkv?key=nombre&value=Juan
```

2. Obtener el valor de una llave existente
```
GET /getkv?key=nombre
```

3. Obtener el valor de una llave inexistente
```
GET /getkv?key=apellido
```

# Link video de funcionamiento 
https://pruebacorreoescuelaingeduco-my.sharepoint.com/:v:/g/personal/juan_brodriguez_mail_escuelaing_edu_co/EWiWYh9T8BhFnJzdplhRanYBeRUiSgjjpnF6fqYytIMPCQ?nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0NvcHkifX0&e=IMc1S9
