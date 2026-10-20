package com.mallcloud.notice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mallcloud.api.common.event.NoticeSendEvent;
import com.mallcloud.api.notice.dto.NoticeDTO;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.notice.entity.NoticeLog;
import com.mallcloud.notice.entity.NoticeTemplate;
import com.mallcloud.notice.mapper.NoticeLogMapper;
import com.mallcloud.notice.mapper.NoticeTemplateMapper;
import com.mallcloud.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 通知服务实现。
 * <p>
 * 渠道适配故意留成一个 switch 而非引入一堆 SDK：短信/邮件服务商更换频繁，
 * 真正接入时在 {@link #doSend} 里替换成具体客户端即可，上层业务不受影响。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeTemplateMapper templateMapper;
    private final NoticeLogMapper logMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean send(NoticeDTO dto) {
        return doSend(dto.userId(), dto.channel(), dto.template(), dto.params());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean send(NoticeSendEvent event) {
        return doSend(event.userId(), event.channel(), event.template(), event.params());
    }

    @Override
    public List<NoticeLogView> listSiteNotice(Long userId, int limit) {
        return logMapper.selectList(Wrappers.<NoticeLog>lambdaQuery()
                        .eq(NoticeLog::getUserId, userId)
                        .eq(NoticeLog::getChannel, "SITE")
                        .eq(NoticeLog::getStatus, 1)
                        .orderByDesc(NoticeLog::getId)
                        .last("limit " + Math.max(limit, 1)))
                .stream()
                .map(log -> new NoticeLogView(log.getId(), log.getTitle(), log.getContent(), log.getCreateTime()))
                .toList();
    }

    private boolean doSend(Long userId, String channel, String templateCode, Map<String, Object> params) {
        if (templateCode == null) {
            throw new BizException(ResultCode.NOTICE_SEND_FAILED, "模板编码不能为空");
        }
        NoticeTemplate template = templateMapper.selectOne(Wrappers.<NoticeTemplate>lambdaQuery()
                .eq(NoticeTemplate::getCode, templateCode)
                .eq(NoticeTemplate::getStatus, 1)
                .last("limit 1"));

        String title = template == null ? templateCode : render(template.getTitle(), params);
        String content = template == null ? templateCode : render(template.getContent(), params);
        String finalChannel = channel != null ? channel : (template == null ? "SITE" : template.getChannel());

        NoticeLog entity = new NoticeLog();
        entity.setUserId(userId);
        entity.setChannel(finalChannel);
        entity.setTemplate(templateCode);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setStatus(0);

        try {
            // TODO 接入真实渠道：SMS → 阿里云/腾讯云短信；EMAIL → JavaMailSender
            //  SITE 渠道只需落库，用户登录后在消息中心查看
            entity.setStatus(1);
            logMapper.insert(entity);
            if (template == null) {
                log.warn("通知模板不存在，已按原始编码发送 template={}", templateCode);
            }
            return true;
        } catch (Exception e) {
            entity.setStatus(2);
            entity.setErrorMsg(e.getMessage());
            logMapper.insert(entity);
            log.error("通知发送失败 template={} userId={}", templateCode, userId, e);
            return false;
        }
    }

    /** 用 {key} 占位替换；参数缺失时保留占位符，便于发现模板问题 */
    private String render(String template, Map<String, Object> params) {
        if (template == null || params == null || params.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}",
                    entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return result;
    }
}
