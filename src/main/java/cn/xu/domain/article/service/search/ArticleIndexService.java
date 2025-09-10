//package cn.xu.domain.article.service.search;
//
//import cn.xu.application.common.ResponseCode;
//import cn.xu.domain.article.model.entity.ArticleEntity;
//import cn.xu.domain.article.service.impl.ArticleService;
//import cn.xu.infrastructure.common.exception.BusinessException;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
//import org.apache.lucene.document.*;
//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TopDocs;
//import org.apache.lucene.search.highlight.Highlighter;
//import org.apache.lucene.search.highlight.QueryScorer;
//import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.MMapDirectory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.ZoneOffset;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
///**
// * 构建文章的索引
// */
//@Slf4j
//@Service
//public class ArticleIndexService {
//
//    @Value("${lucene.index.path:/data/lucene/article}")
//    private String indexPath;
//
//    @Resource
//    private ArticleService articleService;
//
//    private Directory directory;
//    private final Analyzer analyzer = new SmartChineseAnalyzer();
//    private volatile IndexSearcher searcher;
//    private volatile DirectoryReader reader;
//    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
//    private final Object indexLock = new Object();
//
//    @PostConstruct
//    public void init() throws IOException {
//        Path path = Paths.get(indexPath);
//        if (!Files.exists(path)) {
//            Files.createDirectories(path);
//        }
//        this.directory = MMapDirectory.open(path);
//
//        // 初始化索引
//        try {
//            if (DirectoryReader.indexExists(directory)) {
//                reader = DirectoryReader.open(directory);
//                searcher = new IndexSearcher(reader);
//            }
//            // 初始化时重建索引
//            rebuildIndex();
//        } catch (IOException e) {
//            log.error("初始化索引失败", e);
//            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "初始化索引失败");
//        }
//    }
//
//    @PreDestroy
//    public void destroy() {
//        try {
//            if (reader != null) {
//                reader.close();
//            }
//            if (directory != null) {
//                directory.close();
//            }
//        } catch (IOException e) {
//            log.error("关闭索引失败", e);
//        }
//    }
//
//    public void addToIndex(ArticleEntity article) {
//        synchronized (indexLock) {
//            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
//                Document doc = createDocument(article);
//                writer.addDocument(doc);
//                writer.commit();
//                refreshSearcher();
//            } catch (IOException e) {
//                log.error("添加文章索引失败", e);
//                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "添加文章索引失败");
//            }
//        }
//    }
//
//    public void updateIndex(ArticleEntity article) {
//        synchronized (indexLock) {
//            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
//                Document doc = createDocument(article);
//                writer.updateDocument(new Term("id", String.valueOf(article.getId())), doc);
//                writer.commit();
//                refreshSearcher();
//            } catch (IOException e) {
//                log.error("更新文章索引失败", e);
//                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新文章索引失败");
//            }
//        }
//    }
//
//    public void deleteFromIndex(Long articleId) {
//        synchronized (indexLock) {
//            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
//                writer.deleteDocuments(new Term("id", String.valueOf(articleId)));
//                writer.commit();
//                refreshSearcher();
//            } catch (IOException e) {
//                log.error("删除文章索引失败", e);
//                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文章索引失败");
//            }
//        }
//    }
//
//    public List<ArticleEntity> searchArticles(String keyword) {
//        if (searcher == null) {
//            log.warn("搜索器未初始化，尝试重建索引");
//            try {
//                rebuildIndex();
//            } catch (Exception e) {
//                log.error("重建索引失败", e);
//                return new ArrayList<>();
//            }
//        }
//
//        try {
//            log.info("开始搜索文章，关键词: {}", keyword);
//            QueryParser parser = new QueryParser("title", analyzer);
//            Query query = parser.parse(QueryParser.escape(keyword));
//
//            TopDocs results = searcher.search(query, 10);
//            log.info("搜索到{}条结果", results.totalHits.value);
//
//            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
//            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
//
//            List<ArticleEntity> articles = new ArrayList<>();
//            for (ScoreDoc scoreDoc : results.scoreDocs) {
//                Document doc = searcher.doc(scoreDoc.doc);
//                String title = doc.get("title");
//                String highlightedTitle = highlighter.getBestFragment(analyzer, "title", title);
//                log.info("处理搜索结果: 文章ID={}, 标题={}", doc.get("id"), title);
//
//                ArticleEntity article = new ArticleEntity();
//                article.setId(Long.valueOf(doc.get("id")));
//                article.setTitle(highlightedTitle != null ? highlightedTitle : title);
//                article.setDescription(doc.get("description"));
//                articles.add(article);
//            }
//            return articles;
//        } catch (Exception e) {
//            log.error("搜索文章失败: {}", e.getMessage(), e);
//            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "搜索文章失败");
//        }
//    }
//
//    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
//    public void rebuildIndex() {
//        log.info("开始重建文章索引");
//        synchronized (indexLock) {
//            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
//                // 清空所有索引
//                writer.deleteAll();
//
//                // 重新创建索引
//                List<ArticleEntity> articles = articleService.getAllPublishedArticles();
//                log.info("获取到所有文章数量: {}", articles.size());
//                for (ArticleEntity article : articles) {
//                    Document doc = createDocument(article);
//                    writer.addDocument(doc);
//                    log.info("添加文章到索引: {}", article.getTitle());
//                }
//                writer.commit();
//                refreshSearcher();
//                log.info("文章索引重建完成，共索引{}篇文章", articles.size());
//            } catch (IOException e) {
//                log.error("重建文章索引失败", e);
//                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "重建文章索引失败");
//            }
//        }
//    }
//
//    private Document createDocument(ArticleEntity article) {
//        Document doc = new Document();
//        // 使用TextField而不是StringField，因为TextField支持分词
//        doc.add(new StringField("id", String.valueOf(article.getId()), Field.Store.YES));
//        doc.add(new TextField("title", article.getTitle(), Field.Store.YES));
//        doc.add(new TextField("description", article.getDescription(), Field.Store.YES));
//        if (article.getUpdateTime() != null) {
//            long updateTimeMillis = article.getUpdateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
//            doc.add(new StoredField("updateTime", updateTimeMillis));
//        }
//        log.info("创建文档: ID={}, 标题={}", article.getId(), article.getTitle());
//        return doc;
//    }
//
//    private void refreshSearcher() throws IOException {
//        lock.writeLock().lock();
//        try {
//            DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
//            if (newReader != null) {
//                if (reader != null) {
//                    reader.close();
//                }
//                reader = newReader;
//                searcher = new IndexSearcher(reader);
//            }
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//}