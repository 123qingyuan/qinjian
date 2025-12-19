package main.java.com.elderly.monitoring.device.controller;

import main.java.com.elderly.monitoring.device.entity.Device;
import main.java.com.elderly.monitoring.device.entity.DeviceStatus;
import main.java.com.elderly.monitoring.device.entity.DeviceType;
import main.java.com.elderly.monitoring.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 设备管理控制器
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 创建设备
     * 
     * @param device 设备信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> createDevice(@RequestBody Device device) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Device createdDevice = deviceService.createDevice(device);
            response.put("success", true);
            response.put("message", "设备创建成功");
            response.put("data", createdDevice);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取设备详情
     * 
     * @param id 设备ID
     * @return 设备信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> getDeviceById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Device> device = deviceService.findById(id);
        if (device.isPresent()) {
            response.put("success", true);
            response.put("data", device.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据设备ID获取设备信息
     * 
     * @param deviceId 设备ID
     * @return 设备信息
     */
    @GetMapping("/device-id/{deviceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> getDeviceByDeviceId(@PathVariable String deviceId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Device> device = deviceService.findByDeviceId(deviceId);
        if (device.isPresent()) {
            response.put("success", true);
            response.put("data", device.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新设备信息
     * 
     * @param id 设备ID
     * @param updatedDevice 更新的设备信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> updateDevice(
            @PathVariable Long id, 
            @RequestBody Device updatedDevice) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Device device = deviceService.updateDevice(id, updatedDevice);
            response.put("success", true);
            response.put("message", "设备信息更新成功");
            response.put("data", device);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除设备
     * 
     * @param id 设备ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteDevice(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = deviceService.deleteDevice(id);
        if (success) {
            response.put("success", true);
            response.put("message", "设备删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取设备列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向
     * @return 设备列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> getDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order) {
        
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        
        Page<Device> devices = deviceService.findAllDevices(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devices);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 搜索设备
     * 
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param status 设备状态
     * @param userId 用户ID
     * @param isOnline 是否在线
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> searchDevices(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) DeviceType deviceType,
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isOnline,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Device> devices = deviceService.searchDevices(deviceId, deviceName, deviceType, status, userId, isOnline, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devices);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户ID获取设备
     * 
     * @param userId 用户ID
     * @return 设备列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<Map<String, Object>> getDevicesByUserId(@PathVariable Long userId) {
        List<Device> devices = deviceService.findByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devices);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据设备类型获取设备
     * 
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @GetMapping("/type/{deviceType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> getDevicesByType(@PathVariable DeviceType deviceType) {
        List<Device> devices = deviceService.findByDeviceType(deviceType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devices);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据设备状态获取设备
     * 
     * @param status 设备状态
     * @return 设备列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> getDevicesByStatus(@PathVariable DeviceStatus status) {
        List<Device> devices = deviceService.findByStatus(status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devices);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新设备在线状态
     * 
     * @param deviceId 设备ID
     * @param statusRequest 状态请求
     * @return 更新结果
     */
    @PutMapping("/{deviceId}/online-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> updateOnlineStatus(
            @PathVariable String deviceId,
            @RequestBody Map<String, Boolean> statusRequest) {
        
        Boolean isOnline = statusRequest.get("isOnline");
        boolean success = deviceService.updateDeviceOnlineStatus(deviceId, isOnline);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "设备在线状态更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新设备心跳
     * 
     * @param deviceId 设备ID
     * @return 更新结果
     */
    @PostMapping("/{deviceId}/heartbeat")
    public ResponseEntity<Map<String, Object>> updateHeartbeat(@PathVariable String deviceId) {
        boolean success = deviceService.updateDeviceHeartbeat(deviceId);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "心跳更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新设备位置
     * 
     * @param deviceId 设备ID
     * @param locationRequest 位置请求
     * @return 更新结果
     */
    @PutMapping("/{deviceId}/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @PathVariable String deviceId,
            @RequestBody Map<String, Object> locationRequest) {
        
        Double latitude = (Double) locationRequest.get("latitude");
        Double longitude = (Double) locationRequest.get("longitude");
        String locationAddress = (String) locationRequest.get("locationAddress");
        
        boolean success = deviceService.updateDeviceLocation(deviceId, latitude, longitude, locationAddress);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "设备位置更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新设备电量
     * 
     * @param deviceId 设备ID
     * @param batteryRequest 电量请求
     * @return 更新结果
     */
    @PutMapping("/{deviceId}/battery")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> updateBattery(
            @PathVariable String deviceId,
            @RequestBody Map<String, Integer> batteryRequest) {
        
        Integer batteryLevel = batteryRequest.get("batteryLevel");
        boolean success = deviceService.updateDeviceBattery(deviceId, batteryLevel);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "设备电量更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 绑定设备到用户
     * 
     * @param deviceId 设备ID
     * @param bindRequest 绑定请求
     * @return 绑定结果
     */
    @PostMapping("/{deviceId}/bind")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> bindDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, Object> bindRequest) {
        
        Long userId = ((Number) bindRequest.get("userId")).longValue();
        String userName = (String) bindRequest.get("userName");
        
        boolean success = deviceService.bindDeviceToUser(deviceId, userId, userName);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "设备绑定成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 解绑设备
     * 
     * @param deviceId 设备ID
     * @return 解绑结果
     */
    @PostMapping("/{deviceId}/unbind")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> unbindDevice(@PathVariable String deviceId) {
        boolean success = deviceService.unbindDevice(deviceId);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "设备解绑成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "设备不存在");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取设备统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> getDeviceStatistics() {
        DeviceService.DeviceStatistics statistics = deviceService.getDeviceStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", statistics);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取低电量设备列表
     * 
     * @return 低电量设备列表
     */
    @GetMapping("/low-battery")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY_MEMBER')")
    public ResponseEntity<Map<String, Object>> getLowBatteryDevices() {
        List<Device> devices = deviceService.getLowBatteryDevices();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devices);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取需要维护的设备列表
     * 
     * @return 需要维护的设备列表
     */
    @GetMapping("/maintenance-needed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> getDevicesNeedingMaintenance() {
        List<Device> devices = deviceService.getDevicesNeedingMaintenance();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devices);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 处理心跳超时设备
     * 
     * @return 处理结果
     */
    @PostMapping("/handle-timeout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> handleHeartbeatTimeout() {
        int count = deviceService.handleHeartbeatTimeout();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "心跳超时设备处理完成");
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 验证设备ID是否可用
     * 
     * @param deviceId 设备ID
     * @return 验证结果
     */
    @GetMapping("/check-device-id")
    public ResponseEntity<Map<String, Object>> checkDeviceId(@RequestParam String deviceId) {
        Optional<Device> device = deviceService.findByDeviceId(deviceId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", !device.isPresent());
        
        return ResponseEntity.ok(response);
    }
}