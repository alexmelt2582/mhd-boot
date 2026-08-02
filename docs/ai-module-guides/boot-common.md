# boot-common

## 1. Module Purpose
`boot-common` is the foundational shared module for the repository. It provides standardized response objects, business exceptions, error codes, and common utilities for JSON/XML, files, dates, strings, collections, encryption, and ID generation.

## 2. Main Components
- `com.mhd.boot.common.responsedata.BaseResponse<T>`
  - Standard API response wrapper with `code`, `data`, and `msg`
- `com.mhd.boot.common.responsedata.BaseResultUtils`
  - Factory-style helpers for success/error responses
- `com.mhd.boot.common.exception.BusinessException`
  - Business-level runtime exception carrying a stable code
- `com.mhd.boot.common.enums.ErrorCodeEnum`
  - Shared error code catalog
- `com.mhd.boot.common.utils.xml.XmlUtils`
  - JAXB-based XML conversion and XSD validation with internal caches
- `com.mhd.boot.common.utils.json.JsonUtils`
  - JSON serialization/deserialization helper
- `com.mhd.boot.common.utils.SnowflakeIdGenerator`
  - Local snowflake-style ID generation

## 3. Boundaries and Non-Goals
This module does:
- unify response and exception semantics
- provide reusable conversion and utility helpers
- hold shared constants and enums used by upper layers

This module does not:
- implement business workflows
- decide domain validation rules
- define UI/view response structures beyond the shared API wrapper
- replace richer libraries for specialized crypto or XML processing

## 4. Runtime Behavior
1. Service or controller detects a business rule failure
2. Throw `BusinessException` with an `ErrorCodeEnum`
3. Web layer converts the exception into `BaseResponse`
4. Utility classes are used directly where a small stateless helper is enough
5. XML operations use cached `JAXBContext` and cached `Schema` objects to avoid repeated initialization

## 5. Validation and Conversion Rules
`XmlUtils` supports:
- object to XML via `toXml`
- XML to object via `fromXml`
- custom SOAP header assembly via `buildCustomSoapHeader`
- XSD validation via `validateXml`
- schema loading from classpath, file, stream, or bytes

`BaseResultUtils` supports:
- success without payload
- success with payload
- error with code/message
- error with payload
- default message fallback when the caller does not provide a message

## 6. Exception Model
Public-facing business exceptions should use `BusinessException` and `ErrorCodeEnum`.

Typical error categories:
- `ERROR_500`: server-side unknown error
- `ERROR_400`: bad request
- `SUCCESS`: successful operation
- `FAIL`: generic failure
- `VALID_FAILED`: parameter validation failure

Utility failures from XML/JSON conversion are runtime exceptions and should be handled at the boundary layer.

## 7. Usage Guidance
Recommended:
- keep `BaseResponse` as the standard response type for APIs
- keep `BusinessException` as the standard business error carrier
- re-use `ErrorCodeEnum` instead of inventing duplicate code systems
- treat `XmlUtils` as a utility boundary, not a place for business rules

Be careful with:
- null or blank messages, because utilities may fall back to default text
- XML schema paths, because `getSchema` and `getSchemaByFile` fail fast when the resource does not exist
- large object serialization, because the utilities are stateless but memory pressure still matters

## 8. Minimal Example
```java
@GetMapping("/{id}")
public BaseResponse<UserDTO> getUser(@PathVariable Long id) {
    if (id == null || id <= 0) {
        throw new BusinessException(ErrorCodeEnum.VALID_FAILED, "id is invalid");
    }
    UserDTO dto = userService.findById(id);
    return BaseResultUtils.successOfData(dto);
}
```

## 9. Extended Example
```java
public String exportUserXml(UserDTO dto) {
    String xml = XmlUtils.toXml(dto, true, true);
    Schema schema = XmlUtils.getSchema("schema/user.xsd");
    if (!XmlUtils.validateXml(xml, schema)) {
        throw new BusinessException(ErrorCodeEnum.FAIL, "Generated XML does not match XSD");
    }
    return xml;
}
```

## 10. AI Reading Guidance
- Prefer `BaseResultUtils` and `BusinessException` before introducing any new response or exception abstraction.
- If the task mentions XML, check `XmlUtils` first instead of adding a second XML helper.
- If the task needs a new error code, extend `ErrorCodeEnum` rather than hardcoding a string in business code.

## 11. Testing Notes
- `libs/boot-common/src/test/java/com/mhd/boot/common/utils/xml/XmlUtilsDemo.java` shows object/XML round-trip, SOAP header assembly, and XSD validation usage.
- Treat the demo as a behavior reference, not as a required pre-read before using the module in business code.
