-- =============================================================
-- Technology master data seed
-- Reflects the tech tree defined in 03_technology_tree.md
-- =============================================================

-- ── Tier 0: 基礎技術 ──────────────────────────────────────────
INSERT INTO technologies (id, name, category, tier, research_time_seconds, research_cost, weight_modifier, description) VALUES
('T0_INDUSTRY', '基礎工業',  'INDUSTRY',  0, 3600,   50, 1.0, '工業化の第一歩。重工業・電子工学の研究を解禁する。'),
('T0_MILITARY', '基礎軍事',  'MILITARY',  0, 3600,   50, 1.0, '軍事組織の基盤。弾道学・装甲技術を解禁する。'),
('T0_SCIENCE',  '基礎科学',  'SCIENCE',   0, 3600,   50, 1.0, '近代科学の礎。原子力基礎・コンピューティングを解禁する。');

-- ── Tier 1: 応用技術 ──────────────────────────────────────────
INSERT INTO technologies (id, name, category, tier, research_time_seconds, research_cost, weight_modifier, description) VALUES
('T1_HEAVY_IND',    '重工業',       'INDUSTRY',  1, 14400, 150, 1.0, '大規模工場・製鉄所の整備。生産力が向上する。'),
('T1_ELECTRONICS',  '電子工学',     'SCIENCE',   1, 14400, 150, 1.0, '電子機器の研究開発。通信技術・制御システムの基盤。'),
('T1_BALLISTICS',   '弾道学',       'MILITARY',  1, 14400, 150, 1.2, '弾道計算と射撃精度の向上。ミサイル技術への橋渡し。'),
('T1_ARMOR',        '装甲技術',     'MILITARY',  1, 14400, 150, 1.0, '防護素材と装甲設計。防御力とステルス技術の基盤。'),
('T1_NUCLEAR',      '原子力基礎',   'SCIENCE',   1, 21600, 200, 0.8, '核分裂の制御技術。エネルギーと兵器の両面で活用。'),
('T1_COMPUTING',    'コンピューティング', 'SCIENCE', 1, 14400, 150, 1.0, '計算機科学の発展。AI研究・シミュレーション能力を向上。');

-- ── Tier 2: 先進技術 ──────────────────────────────────────────
INSERT INTO technologies (id, name, category, tier, research_time_seconds, research_cost, weight_modifier, description) VALUES
('T2_AUTOMATION',   '自動化',           'INDUSTRY',  2, 43200, 400, 1.0, '生産工程の自動化。巨大工場建設の前提技術。'),
('T2_MISSILE',      'ミサイル技術',     'MILITARY',  2, 43200, 500, 1.2, '誘導ミサイルの開発。ICBM開発への直接的な前提。'),
('T2_STEALTH',      'ステルス技術',     'MILITARY',  2, 43200, 450, 0.9, '電波・熱源遮蔽技術。巨大要塞の防御に応用。'),
('T2_NUCLEAR_ENG',  '原子力工学',       'SCIENCE',   2, 64800, 600, 0.7, '核エネルギーの実用化。ICBM核弾頭開発の必須技術。'),
('T2_AI',           'AI研究',           'SCIENCE',   2, 43200, 500, 0.9, '人工知能の軍事・民間応用。宇宙港管制システムへ発展。'),
('T2_NANO',         'ナノテクノロジー', 'SCIENCE',   2, 64800, 600, 0.7, '分子レベルの素材制御。ナノマシン製造施設の前提。');

-- ── Tier 3: メガテック ────────────────────────────────────────
INSERT INTO technologies (id, name, category, tier, research_time_seconds, research_cost, weight_modifier, description) VALUES
('T3_ICBM',         'ICBM開発',             'MILITARY',  3, 604800, 2000, 0.6, '大陸間弾道ミサイルの開発。ICBMサイロ建設を解禁する。'),
('T3_MEGA_FACTORY', '巨大工場',             'INDUSTRY',  3, 604800, 1800, 0.7, '全生産+25%のメガ工場コンプレックス。'),
('T3_SPACE_PORT',   '宇宙港',               'SCIENCE',   3, 604800, 2000, 0.5, '宇宙技術と国家威信の象徴。宇宙系技術を解禁。'),
('T3_NANO_FAB',     'ナノマシン製造施設',   'SCIENCE',   3, 604800, 1800, 0.5, '先進素材の量産施設。'),
('T3_FORTRESS',     '巨大要塞',             'MILITARY',  3, 604800, 1800, 0.6, '防御力大幅UP。ICBMサイロの前提となる軍事メガ建造物。');

-- ── Prerequisites ─────────────────────────────────────────────
-- Tier 0 → Tier 1
INSERT INTO technology_prerequisites (technology_id, prerequisite_id) VALUES
('T1_HEAVY_IND',   'T0_INDUSTRY'),
('T1_ELECTRONICS', 'T0_INDUSTRY'),
('T1_BALLISTICS',  'T0_MILITARY'),
('T1_ARMOR',       'T0_MILITARY'),
('T1_NUCLEAR',     'T0_SCIENCE'),
('T1_COMPUTING',   'T0_SCIENCE');

-- Tier 1 → Tier 2
INSERT INTO technology_prerequisites (technology_id, prerequisite_id) VALUES
('T2_AUTOMATION',  'T1_HEAVY_IND'),
('T2_MISSILE',     'T1_BALLISTICS'),
('T2_STEALTH',     'T1_ARMOR'),
('T2_NUCLEAR_ENG', 'T1_NUCLEAR'),
('T2_AI',          'T1_COMPUTING'),
('T2_NANO',        'T1_ELECTRONICS');

-- Tier 2 → Tier 3
INSERT INTO technology_prerequisites (technology_id, prerequisite_id) VALUES
('T3_ICBM',         'T2_MISSILE'),
('T3_ICBM',         'T2_NUCLEAR_ENG'),
('T3_MEGA_FACTORY', 'T2_AUTOMATION'),
('T3_SPACE_PORT',   'T2_AI'),
('T3_NANO_FAB',     'T2_NANO'),
('T3_FORTRESS',     'T2_STEALTH');
