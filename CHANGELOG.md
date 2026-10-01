# Changelog

## 1.1.0

- Add task IDs and argument keys for density, animation scales, connectivity,
  private DNS, firewall, package permission inspection, and force-stop.
- Open mutating-task confirmation from the foreground client instead of the
  manager's background service, which Android blocks.
- Handle non-terminal `APPROVAL_REQUIRED` without removing the task callback.
- Raise request SDK version to 2; retain Binder protocol version 2.
- Require an updated manager implementing the maintenance catalog. The SDK
  cannot add tasks to older manager versions.
- Verify density and animation changes on Xiaomi Watch 2 through the example
  apps, manager confirmation, and ADB read-back.
