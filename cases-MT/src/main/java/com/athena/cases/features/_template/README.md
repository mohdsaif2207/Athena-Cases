# Feature module template (backend)

Copy this package tree when adding a new case type:

```
features/<module>/
  controller/
  service/
  repository/
  entity/
  dto/
  mapper/
  validation/
  client/
  config/
  exception/
```

Rules: see `docs/architecture/06_Feature_Module_Template.md`.

Do **not** put business logic in `_template`. It is a structural placeholder only.
