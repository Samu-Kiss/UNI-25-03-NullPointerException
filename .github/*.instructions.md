# PR Review Rules (for Copilot)

Objetivo: verificar arquitectura por capas, consistencia de idiomas, Javadoc descriptivo en español, comentarios/debug útiles y uso correcto de inyección de dependencias (DI).
Capas: view → controller → service → repository → entities.

## 1. Arquitectura (capas y dependencias)

Validar:

* view solo habla con controller. Sin lógica de negocio ni acceso a repos.
* controller orquesta/valida y llama a service. Sin SQL/IO directo ni mapeo pesado.
* service contiene la lógica de negocio. Llama a repository y usa entities/DTOs/mappers.
* repository accede a datos. Sin reglas de negocio ni referencias a view/controller.
* entities sin dependencia a frameworks o capas superiores (POJOs/records).

Bloquear:

* saltos de capa (p. ej., controller → repository directo, view → service),
* acoplamientos circulares,
* lógica de negocio en controller/repository,
* entities acopladas a infraestructura.

Si hay violación, sugerir patrón aplicable (según contexto):

* Adapter/Facade para aislar infraestructura o simplificar orquestación en controller/service.
* Strategy/Policy cuando haya variantes de negocio.
* Command para operaciones compuestas de UI o casos de uso.
* Factory/Abstract Factory para instanciación desacoplada.
* Decorator para cross-cutting (cache, métricas) sin tocar la lógica central.
* Observer/Domain Events para reacciones desacopladas.
* Mapper (manual/MapStruct) para Entities ↔ DTOs.

## 2. Inyección de dependencias (DI)

Validar:

* inyección por constructor (preferida). Evitar field injection.
* dependencias hacia **interfaces/puertos** (no a implementaciones concretas).
* sin Service Locator ni `ApplicationContext.getBean()` en producción.
* sin `new` de repos/servicios dentro de controller/service (excepto fábricas legítimas).
* dependencias marcadas como `final` cuando aplique.
* configuración (p. ej., timeouts, URLs) entra por DI/config bindings, no literales dispersos.
* sin singletons estáticos para recursos compartidos; usar el contenedor de DI.
* tests: dobles (mocks/fakes) inyectados por constructor; sin tocar el wiring de producción.

Señales de alerta:

* `new XRepository()` en controller/service.
* anotaciones de campo tipo `@Autowired` en atributos (preferir constructor).
* clases con demasiadas dependencias (posible violación SRP o falta de Facade).
* ciclos de dependencias detectados por el contenedor.

## 3. Idiomas (solo idioma, no formato/casing)

* clases, interfaces y métodos en **inglés** (e.g., `UserService`, `createUser`, `findById`).
* atributos/campos en **español** (e.g., `nombreCompleto`, `fechaCreacion`, `esActivo`).
* constantes: idioma según semántica global; sugerir **inglés** si la API es pública.
* logs: consistente; preferible **inglés** si es API pública/multi-equipo.

## 4. Javadoc (descriptivo en español)

* toda clase y método **público** con Javadoc en **español**.
* describir propósito/contrato/reglas, no repetir nombres de parámetros.
* incluir `@param`, `@return`, `@throws` con descripciones concretas.
* en services: reglas de negocio y efectos colaterales.
* en controllers: validaciones, códigos/errores relevantes.
* en repositories: criterios/garantías (filtros, orden, paginación).

Ejemplo breve:

```java
/**
 * Obtiene la representación pública de un usuario.
 *
 * Reglas: oculta campos sensibles y normaliza el correo.
 *
 * @param userId identificador único del usuario.
 * @return DTO con los datos públicos del usuario.
 * @throws UsuarioNoEncontradoException si no existe el usuario.
 */
public UserDto findById(String userId) { ... }
```

## 5. Comentarios y salidas de debug

* comentarios solo cuando aporten el “por qué” o el enfoque; evitar narrar lo obvio.
* deuda técnica: usar `// TODO:` y `// FIXME:` con contexto y/o ID de issue.
* logging claro y accionable; niveles correctos (`trace/debug/info/warn/error`).
* no incluir datos sensibles en logs; agregar contexto mínimo (IDs, tiempos, tamaños).

## 6. Antipatrones a bloquear

* view → service directo; controller → repository directo.
* lógica de negocio en controller/repository.
* instanciación manual (`new`) de servicios/repos en capas superiores.
* Service Locator / accesos estáticos al contenedor.
* entities dependientes de frameworks.
* métodos públicos sin Javadoc descriptivo.
* logs `System.out/err` en producción o con datos sensibles.

## 7. Checklist de verificación

* [ ] Capas respetadas y sin saltos/ciclos.
* [ ] Lógica de negocio en service; repos solo persistencia; entities sin framework.
* [ ] DI por constructor; dependencias a interfaces; sin `new`/Service Locator.
* [ ] Idioma: clases/métodos en inglés; atributos en español; logs consistentes.
* [ ] Javadoc en español con contrato y `@param/@return/@throws`.
* [ ] Comentarios necesarios y `TODO/FIXME` con contexto; logs claros y seguros.
* [ ] Si hay violación, sugerir patrón (Adapter, Facade, Strategy, Command, Factory, Decorator, Observer, Mapper).

## 8. Plantilla de reporte de revisión (Copilot)

Usa esta plantilla para comentar el PR:

```
## Revisión automática (Copilot)

Arquitectura
- Hallazgos:
  - {archivo}:{línea} → {regla violada, p. ej. controller accede a repository}
- Sugerencias:
  - Mover {lógica/clase} a {capa}; introducir {Patrón} ({Strategy/Adapter/Facade/...}) para {contexto}.

Inyección de dependencias
- Hallazgos:
  - {clase} con field injection / new {Tipo} en {archivo}:{línea} / dependencia concreta
- Sugerencias:
  - Cambiar a inyección por constructor; depender de interfaz {IPuerto}; registrar implementación en el contenedor.

Idiomas
- Inconsistencias:
  - {símbolo} en {archivo}:{línea} → debe estar en {inglés/español}
- Sugerencia:
  - Renombrar a {nuevoNombre} y actualizar referencias.

Javadoc
- Faltantes/insuficientes:
  - {archivo}:{línea} → añadir contrato y @param/@return/@throws con detalles
- Sugerencia:
  - Documentar reglas/validaciones y efectos colaterales.

Comentarios y Debug
- Observaciones:
  - {comentario redundante / log ruidoso / dato sensible}
- Sugerencia:
  - Simplificar al “por qué”; ajustar nivel a {debug/info}; retirar {dato sensible}.

Conclusión
- Estado sugerido: {Aprobar / Solicitar cambios}
- Prioridad: {Alta/Media/Baja}
- Acciones (ordenadas):
  1) {acción arquitectura/DI}
  2) {acción idiomas}
  3) {acción Javadoc/comentarios/logs}
```

---
