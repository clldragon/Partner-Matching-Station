package com.cl.yupao.service.impl;

import com.cl.yupao.model.domain.Tag;
import com.cl.yupao.mapper.TagMapper;
import com.cl.yupao.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 标签 服务实现类
 * </p>
 *
 * @author cl
 * @since 2024-05-29
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
