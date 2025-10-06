package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.file.model.entity.FileEntity;
import cn.xu.domain.file.model.valobj.FileMetadata;
import cn.xu.domain.file.model.valobj.FileName;
import cn.xu.domain.file.model.valobj.FileUrl;
import cn.xu.infrastructure.persistent.po.File;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 * 
 * @author xu
 */
@Component
public class FileConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 文件领域实体
     * @return 文件持久化对象
     */
    public File toDataObject(FileEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return File.builder()
                .fileId(entity.getFileId())
                .originalName(entity.getOriginalName() != null ? entity.getOriginalName().getFullName() : null)
                .systemName(entity.getSystemName() != null ? entity.getSystemName().getFullName() : null)
                .fileUrl(entity.getFileUrl() != null ? entity.getFileUrl().getUrl() : null)
                .storagePath(entity.getStoragePath())
                .fileSize(entity.getMetadata() != null ? entity.getMetadata().getSize() : null)
                .mimeType(entity.getMetadata() != null ? entity.getMetadata().getContentType() : null)
                .fileExtension(entity.getOriginalName() != null ? entity.getOriginalName().getExtension() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .uploadUserId(entity.getUploadUserId())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param po 文件持久化对象
     * @return 文件领域实体
     */
    public FileEntity toDomainEntity(File po) {
        if (po == null) {
            return null;
        }
        
        FileEntity entity = new FileEntity();
        entity.setFileId(po.getFileId());
        
        if (po.getOriginalName() != null) {
            entity.setOriginalName(new FileName(po.getOriginalName()));
        }
        
        if (po.getSystemName() != null) {
            entity.setSystemName(new FileName(po.getSystemName()));
        }
        
        if (po.getFileUrl() != null) {
            entity.setFileUrl(new FileUrl(po.getFileUrl()));
        }
        
        entity.setStoragePath(po.getStoragePath());
        
        if (po.getFileSize() != null || po.getMimeType() != null) {
            FileMetadata metadata = new FileMetadata(
                    po.getFileSize() != null ? po.getFileSize() : 0L,
                    po.getMimeType()
            );
            entity.setMetadata(metadata);
        }
        
        if (po.getStatus() != null) {
            entity.setStatus(FileEntity.FileStatus.valueOf(po.getStatus()));
        }
        
        entity.setUploadUserId(po.getUploadUserId());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        
        return entity;
    }

    /**
     * 批量转换持久化对象为领域实体
     *
     * @param pos 持久化对象列表
     * @return 领域实体列表
     */
    public List<FileEntity> toDomainEntities(List<File> pos) {
        if (pos == null || pos.isEmpty()) {
            return Collections.emptyList();
        }

        return pos.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换领域实体为持久化对象
     *
     * @param entities 领域实体列表
     * @return 持久化对象列表
     */
    public List<File> toDataObjects(List<FileEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}