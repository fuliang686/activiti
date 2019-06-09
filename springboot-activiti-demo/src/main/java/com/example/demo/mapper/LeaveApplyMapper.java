package com.example.demo.mapper;

import com.example.demo.domain.LeaveApply;

/**
 * @author FL
 * @ClassName: com.example.demo.mapper.LeaveApplyMapper
 * @description: TODO
 * @date 2019/6/9 9:49
 */
public interface LeaveApplyMapper {
    void save(LeaveApply apply);
    LeaveApply get(int id);
    void update(LeaveApply app);
}
