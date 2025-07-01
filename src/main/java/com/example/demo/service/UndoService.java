// package com.example.demo.service;

// import com.example.demo.dto.UndoActionDTO;
// import com.example.demo.entity.Bode;
// import com.example.demo.entity.Giaovien;
// import com.example.demo.entity.Sinhvien;

// import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.Stack;
// import java.util.function.Supplier;

// @Service
// public class UndoService {

//     // Map để lưu stack undo action cho từng user
//     private final Map<String, Stack<UndoAction>> userUndoStacks = new HashMap<>();

//     public void pushUndoAction(String userId, UndoAction action) {
//         userUndoStacks.computeIfAbsent(userId, k -> new Stack<>()).push(action);
//     }
    
//     public void clearAllUndoStacks() {
//         userUndoStacks.clear();
//     }    

//     public Object undoLastAction(String userId) {
//         System.out.println("Attempting to undo for user: " + userId);
//         Stack<UndoAction> userStack = userUndoStacks.get(userId);
//         if (userStack == null || userStack.isEmpty()) {
//             System.out.println("No undo stack found for user: " + userId);
//             return null;
//         }

//         UndoAction lastAction = userStack.pop();
//         System.out.println("Executing undo action: " + lastAction.actionType +
//                 " on " + lastAction.entityType + " with ID " + lastAction.entityId);
//                 System.out.println(userId + " " + lastAction.entityId + " " + lastAction.actionType);
//                 // Gọi hàm undoFunction để thực hiện hành động undo
//                 if (lastAction.undoFunction == null) {
//                     System.out.println("No undo function found for action: " + lastAction.actionType);
//                     return null;
//                 }
//                 System.out.println("Undo function found for action: " + lastAction.actionType);
//                 System.out.println("Executing undo function...");
//                 // Supplier<Object> undoFunction = lastAction.undoFunction;
//                 // Object result = undoFunction.get();
//                 // System.out.println("Undo function executed successfully.");
//                 // System.err.println("Undo result: " + result);
//                 return lastAction.undoFunction.get();
//     }

//     public boolean canUndo(String userId) {
//         Stack<UndoAction> userStack = userUndoStacks.get(userId);
//         return userStack != null && !userStack.isEmpty();
//     }

//     public UndoActionDTO getLastUndoAction(String userId) {
//         Stack<UndoAction> userStack = userUndoStacks.get(userId);
//         if (userStack == null || userStack.isEmpty()) {
//             return null;
//         }

//         UndoAction lastAction = userStack.peek();
//         UndoActionDTO dto = new UndoActionDTO();
//         dto.setEntityType(lastAction.entityType);
//         dto.setActionType(lastAction.actionType);
//         dto.setEntityId(lastAction.entityId);

//         // Xử lý tên entity dựa trên loại đối tượng
//        if (lastAction.entityType.equals("BODE")) {
//             if (lastAction.originalState instanceof Bode) {
//                 Bode bode = (Bode) lastAction.originalState;
//                 dto.setEntityName("Câu hỏi " + bode.getId().getCauHoi() + " - " + bode.getMonhoc().getMaMH());
//             } else if (lastAction.newState instanceof Bode) {
//                 Bode bode = (Bode) lastAction.newState;
//                 dto.setEntityName("Câu hỏi " + bode.getId().getCauHoi() + " - " + bode.getMonhoc().getMaMH());
//             } else {
//                 dto.setEntityName(lastAction.entityId);
//             }
//         } else if (lastAction.entityType.equals("GIAOVIEN")) {
//             if (lastAction.originalState instanceof Giaovien) {
//                 dto.setEntityName(((Giaovien) lastAction.originalState).getHoTen());
//             } else if (lastAction.newState instanceof Giaovien) {
//                 dto.setEntityName(((Giaovien) lastAction.newState).getHoTen());
//             } else {
//                 dto.setEntityName(lastAction.entityId);
//             }
//         } else if (lastAction.entityType.equals("SINHVIEN")) {
//             // Thêm xử lý cho sinh viên
//             if (lastAction.originalState instanceof Sinhvien) {
//                 Sinhvien sinhvien = (Sinhvien) lastAction.originalState;
//                 dto.setEntityName(sinhvien.getHoTen());
//             } else if (lastAction.newState instanceof Sinhvien) {
//                 Sinhvien sinhvien = (Sinhvien) lastAction.newState;
//                 dto.setEntityName(sinhvien.getHoTen());
//             } else {
//                 dto.setEntityName(lastAction.entityId);
//             }
//         } else {
//             dto.setEntityName(lastAction.entityId);
//         }

//         dto.setTimestamp(LocalDateTime.now());
//         return dto;
//     }
    

//     // Inner class for undo action
//     public static class UndoAction {
//         private final String entityType;
//         private final String actionType;
//         private final String entityId;
//         private final Object originalState;
//         private final Object newState;
//         private final Supplier<Object> undoFunction;

//         public UndoAction(String entityType, String actionType, String entityId,
//                 Object originalState, Object newState,
//                 Supplier<Object> undoFunction) {
//             this.entityType = entityType;
//             this.actionType = actionType;
//             this.entityId = entityId;
//             this.originalState = originalState;
//             this.newState = newState;
//             this.undoFunction = undoFunction;
//         }
//     }
// }
package com.example.demo.service;

import com.example.demo.dto.UndoActionDTO;
import com.example.demo.entity.Bode;
import com.example.demo.entity.Giaovien;
import com.example.demo.entity.Sinhvien;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

@Service
public class UndoService {

    // Cấu trúc mới: Map<userId, Map<entityType, Stack<UndoAction>>>
    private final Map<String, Map<String, Stack<UndoAction>>> userEntityUndoStacks = new HashMap<>();

    /**
     * Thêm một hành động vào stack undo của user và entity type cụ thể
     * 
     * @param userId     người dùng thực hiện hành động
     * @param entityType loại đối tượng (tab) đang được thao tác
     * @param action     hành động được thực hiện
     */
    public void pushUndoAction(String userId, String entityType, UndoAction action) {
        // Lấy map cho user này, nếu chưa có thì tạo mới
        Map<String, Stack<UndoAction>> userStacks = userEntityUndoStacks.computeIfAbsent(userId, k -> new HashMap<>());

        // Lấy stack cho entity type này, nếu chưa có thì tạo mới
        Stack<UndoAction> entityStack = userStacks.computeIfAbsent(entityType, k -> new Stack<>());

        // Thêm hành động vào stack
        entityStack.push(action);
    }

    /**
     * Xóa tất cả undo stacks
     */
    public void clearAllUndoStacks() {
        userEntityUndoStacks.clear();
    }

    /**
     * Xóa undo stack cho một user cụ thể
     * 
     * @param userId ID của người dùng
     */
    public void clearUserUndoStacks(String userId) {
        userEntityUndoStacks.remove(userId);
    }

    /**
     * Xóa undo stack cho một user và entity type cụ thể
     * 
     * @param userId     ID của người dùng
     * @param entityType loại đối tượng (tab)
     */
    public void clearEntityUndoStack(String userId, String entityType) {
        Map<String, Stack<UndoAction>> userStacks = userEntityUndoStacks.get(userId);
        if (userStacks != null) {
            userStacks.remove(entityType);
        }
    }

    /**
     * Hoàn tác hành động cuối cùng của user trên một entity type cụ thể
     * 
     * @param userId     ID của người dùng
     * @param entityType loại đối tượng (tab)
     * @return kết quả của hành động undo
     */
    public Object undoLastAction(String userId, String entityType) {
        // Kiểm tra xem có stack undo cho user và entity này không
        if (!canUndo(userId, entityType)) {
            throw new IllegalStateException("No actions to undo for this entity type");
        }

        // Lấy stack undo cho user và entity
        Map<String, Stack<UndoAction>> userStacks = userEntityUndoStacks.get(userId);
        Stack<UndoAction> entityStack = userStacks.get(entityType);

        // Lấy và xóa hành động cuối cùng khỏi stack
        UndoAction lastAction = entityStack.pop();

        // Nếu stack rỗng, xóa entry cho entity này
        if (entityStack.isEmpty()) {
            userStacks.remove(entityType);
            // Nếu user không còn entity nào, xóa entry cho user này
            if (userStacks.isEmpty()) {
                userEntityUndoStacks.remove(userId);
            }
        }

        // Thực hiện hành động undo và trả về kết quả
        return lastAction.undoFunction.get();
    }

    /**
     * Kiểm tra xem có thể undo cho user và entity type cụ thể không
     * 
     * @param userId     ID của người dùng
     * @param entityType loại đối tượng (tab)
     * @return true nếu có thể undo, false nếu không
     */
    public boolean canUndo(String userId, String entityType) {
        Map<String, Stack<UndoAction>> userStacks = userEntityUndoStacks.get(userId);
        if (userStacks == null) {
            return false;
        }

        Stack<UndoAction> entityStack = userStacks.get(entityType);
        return entityStack != null && !entityStack.isEmpty();
    }

    /**
     * Lấy thông tin về hành động undo cuối cùng cho user và entity type cụ thể
     * 
     * @param userId     ID của người dùng
     * @param entityType loại đối tượng (tab)
     * @return thông tin về hành động undo cuối cùng
     */
    public UndoActionDTO getLastUndoAction(String userId, String entityType) {
        if (!canUndo(userId, entityType)) {
            return null;
        }

        Map<String, Stack<UndoAction>> userStacks = userEntityUndoStacks.get(userId);
        Stack<UndoAction> entityStack = userStacks.get(entityType);
        UndoAction lastAction = entityStack.peek();

        return new UndoActionDTO(
                lastAction.entityType,
                lastAction.actionType,
                lastAction.entityId,
                lastAction.entityName,
                lastAction.timestamp);
    }

    /**
     * Lấy số lượng hành động có thể undo cho user và entity type cụ thể
     * 
     * @param userId     ID của người dùng
     * @param entityType loại đối tượng (tab)
     * @return số lượng hành động có thể undo
     */
    public int getUndoStackSize(String userId, String entityType) {
        if (!canUndo(userId, entityType)) {
            return 0;
        }

        return userEntityUndoStacks.get(userId).get(entityType).size();
    }

    /**
     * Inner class đại diện cho một hành động undo
     */
    public static class UndoAction {
        public final String entityType; // Loại đối tượng (Giaovien, Sinhvien, Monhoc, etc.)
        public final String actionType; // Loại hành động (INSERT, UPDATE, DELETE)
        public final String entityId; // ID của đối tượng
        public final String entityName; // Tên đối tượng (để hiển thị thông báo)
        public final LocalDateTime timestamp; // Thời điểm thực hiện hành động
        public final Supplier<Object> undoFunction; // Hàm thực hiện undo

        public UndoAction(
                String entityType,
                String actionType,
                String entityId,
                String entityName,
                Supplier<Object> undoFunction) {
            this.entityType = entityType;
            this.actionType = actionType;
            this.entityId = entityId;
            this.entityName = entityName;
            this.timestamp = LocalDateTime.now();
            this.undoFunction = undoFunction;
        }
    }
}