-- V13__Expand_Skill_Aliases.sql

-- 1. Tạo bảng (nếu chưa có)
CREATE TABLE IF NOT EXISTS `skill_aliases` (
                                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                                               `alias` VARCHAR(255) NOT NULL,
    `normalized_name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_skill_alias` (`alias`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Chèn bộ từ điển đa ngành (Chuẩn hóa toàn bộ về chữ thường)
INSERT INTO `skill_aliases` (`alias`, `normalized_name`) VALUES
-- --- CÔNG NGHỆ THÔNG TIN (MỞ RỘNG) ---
('ts', 'typescript'),
('py', 'python'),
('ml', 'machine learning'),
('ai', 'artificial intelligence'),
('nlp', 'natural language processing'),
('llm', 'large language model'),
('c#', 'csharp'),
('.net', 'dotnet'),
('ux', 'user experience'),
('ui', 'user interface'),
('qa', 'quality assurance'),
('qc', 'quality control'),
('devops', 'development operations'),

-- --- MARKETING & TRUYỀN THÔNG ---
('seo', 'search engine optimization'),
('sem', 'search engine marketing'),
('ads', 'advertising'),
('smm', 'social media marketing'),
('pr', 'public relations'),
('content', 'content creation'),
('copywriting', 'copy writing'),
('crm', 'customer relationship management'),
('kol', 'key opinion leader'),

-- --- TÀI CHÍNH & KẾ TOÁN ---
('acc', 'accounting'),
('cpa', 'certified public accountant'),
('p&l', 'profit and loss'),
('cfo', 'chief financial officer'),
('tax', 'taxation'),
('audit', 'auditing'),
('banking', 'investment banking'),

-- --- QUẢN TRỊ & KINH DOANH ---
('hr', 'human resources'),
('ba', 'business analyst'),
('pm', 'project manager'),
('po', 'product owner'),
('b2b', 'business to business'),
('b2c', 'business to consumer'),
('kpi', 'key performance indicator'),
('okr', 'objectives and key results'),
('erp', 'enterprise resource planning'),

-- --- NGÔN NGỮ & CHỨNG CHỈ (RẤT QUAN TRỌNG) ---
('ielts', 'international english language testing system'),
('toeic', 'test of english for international communication'),
('hsk', 'hanyu shuiping kaoshi'),
('jlpt', 'japanese language proficiency test'),
('n1', 'japanese n1 level'),
('n2', 'japanese n2 level'),
('n3', 'japanese n3 level'),

-- --- XÂY DỰNG & THIẾT KẾ ---
('cad', 'computer aided design'),
('bim', 'building information modeling'),
('mep', 'mechanical electrical plumbing'),
('ps', 'photoshop'),
('ai_design', 'adobe illustrator'), -- Tránh trùng với AI (Trí tuệ nhân tạo)
('ae', 'after effects'),
('pr_design', 'adobe premiere'); -- Tránh trùng với PR (Public Relations)