-- File ini berisi semua SQL untuk setup database Supabase
-- Jalankan di Supabase SQL Editor

-- 1. Create table pegawai
CREATE TABLE IF NOT EXISTS pegawai (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nip TEXT UNIQUE NOT NULL,
    nama TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'user', -- 'admin' atau 'user'
    email TEXT UNIQUE NOT NULL, -- Email untuk auth (bisa dummy)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE pegawai ENABLE ROW LEVEL SECURITY;

-- Policy: Allow authenticated users to read their own data
CREATE POLICY "Users can read own data" ON pegawai
    FOR SELECT
    USING ((auth.uid())::text = (id)::text) OR (role = ANY (ARRAY['admin'::text, 'user'::text]));

-- Policy: Allow insert for service role (manual insert di dashboard)
CREATE POLICY "Service role can insert" ON pegawai
    FOR INSERT
    WITH CHECK (true);

-- Policy: Allow update for authenticated users
CREATE POLICY "Users can update own data" ON pegawai
    FOR UPDATE
    USING (auth.uid()::text = id::text);

-- Create index untuk performa
CREATE INDEX idx_pegawai_nip ON pegawai(nip);
CREATE INDEX idx_pegawai_email ON pegawai(email);
CREATE INDEX idx_pegawai_role ON pegawai(role);

-- Function untuk auto update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger untuk auto update
CREATE TRIGGER update_pegawai_updated_at
    BEFORE UPDATE ON pegawai
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert data admin contoh
-- PENTING: Setelah insert, buat user di Supabase Auth dengan email yang sama
INSERT INTO pegawai (nip, nama, role, email)
VALUES
('199001012020011001', 'Administrator', 'admin', 'admin@suratapp.local'),
('199101012020012001', 'Budi Santoso', 'user', 'budi.santoso@suratapp.local'),
('199201012020013001', 'Ani Wijaya', 'user', 'ani.wijaya@suratapp.local');

-- View untuk join dengan auth.users (opsional, untuk monitoring)
CREATE OR REPLACE VIEW v_pegawai_auth AS
SELECT
    p.id,
    p.nip,
    p.nama,
    p.role,
    p.email,
    p.created_at,
    CASE
        WHEN au.id IS NOT NULL THEN true
        ELSE false
    END as has_auth_account
FROM pegawai p
LEFT JOIN auth.users au ON p.email = au.email;

-- 2. Create table surat_masuk dan keluar
-- Supabase Database Setup
-- Jalankan script ini di Supabase SQL Editor

-- Table untuk Surat Masuk
CREATE TABLE IF NOT EXISTS surat_masuk (
    id SERIAL PRIMARY KEY,
    pengirim TEXT NOT NULL,
    nomor_surat TEXT NOT NULL,
    tanggal_surat TEXT NOT NULL,
    nomor_agenda TEXT NOT NULL,
    tanggal_diterima TEXT NOT NULL,
    perihal TEXT NOT NULL,
    status_surat TEXT NOT NULL,
    file_url TEXT,
    nama_penerima TEXT,
    tanda_tangan TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Table untuk Surat Keluar
CREATE TABLE IF NOT EXISTS surat_keluar (
    id SERIAL PRIMARY KEY,
    pengirim TEXT NOT NULL,
    nomor_surat TEXT NOT NULL,
    tanggal_surat TEXT NOT NULL,
    nomor_agenda TEXT NOT NULL,
    tanggal_diterima TEXT NOT NULL,
    perihal TEXT NOT NULL,
    status_surat TEXT NOT NULL,
    file_url TEXT,
    nama_penerima TEXT,
    tanda_tangan TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security (RLS)
ALTER TABLE surat_masuk ENABLE ROW LEVEL SECURITY;
ALTER TABLE surat_keluar ENABLE ROW LEVEL SECURITY;

-- Create policies untuk public access (karena tidak menggunakan auth Supabase)
-- Anda bisa menyesuaikan policy ini sesuai kebutuhan keamanan

-- Policy untuk surat_masuk
CREATE POLICY "Enable read access for all users" ON surat_masuk
    FOR SELECT USING (true);

CREATE POLICY "Enable insert access for all users" ON surat_masuk
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Enable update access for all users" ON surat_masuk
    FOR UPDATE USING (true);

CREATE POLICY "Enable delete access for all users" ON surat_masuk
    FOR DELETE USING (true);

-- Policy untuk surat_keluar
CREATE POLICY "Enable read access for all users" ON surat_keluar
    FOR SELECT USING (true);

CREATE POLICY "Enable insert access for all users" ON surat_keluar
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Enable update access for all users" ON surat_keluar
    FOR UPDATE USING (true);

CREATE POLICY "Enable delete access for all users" ON surat_keluar
    FOR DELETE USING (true);

-- Create Storage Bucket untuk file surat
-- Jalankan ini di Supabase Dashboard > Storage
-- atau gunakan SQL berikut:

INSERT INTO storage.buckets (id, name, public)
VALUES ('surat_files', 'surat_files', true)
ON CONFLICT (id) DO NOTHING;

-- Storage policy untuk public access
CREATE POLICY "Public Access"
ON storage.objects FOR SELECT
USING (bucket_id = 'surat_files');

CREATE POLICY "Public Upload"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'surat_files');

CREATE POLICY "Public Update"
ON storage.objects FOR UPDATE
USING (bucket_id = 'surat_files');

CREATE POLICY "Public Delete"
ON storage.objects FOR DELETE
USING (bucket_id = 'surat_files');

-- Index untuk performa lebih baik
CREATE INDEX idx_surat_masuk_created_at ON surat_masuk(created_at);
CREATE INDEX idx_surat_masuk_status ON surat_masuk(status_surat);
CREATE INDEX idx_surat_masuk_tanggal_diterima ON surat_masuk(tanggal_diterima);

CREATE INDEX idx_surat_keluar_created_at ON surat_keluar(created_at);
CREATE INDEX idx_surat_keluar_status ON surat_keluar(status_surat);
CREATE INDEX idx_surat_keluar_tanggal_diterima ON surat_keluar(tanggal_diterima);

-- Sample data (opsional, untuk testing)
INSERT INTO surat_masuk (pengirim, nomor_surat, tanggal_surat, nomor_agenda, tanggal_diterima, perihal, status_surat)
VALUES
('Dinas Pendidikan', '001/DP/2025', '2025-01-15', 'A-001', '2025-01-16', 'Undangan Rapat Koordinasi', 'Sekretaris'),
('Kementerian Dalam Negeri', '002/KDN/2025', '2025-01-14', 'A-002', '2025-01-16', 'Surat Edaran tentang Protokol Kesehatan', 'Kepala');

INSERT INTO surat_keluar (pengirim, nomor_surat, tanggal_surat, nomor_agenda, tanggal_diterima, perihal, status_surat)
VALUES
('Dinas Kesehatan Kota', '003/DKK/2025', '2025-01-15', 'B-001', '2025-01-16', 'Laporan Kegiatan Bulan Desember', 'Sekretaris'),
('Bupati Medan', '004/BP/2025', '2025-01-14', 'B-002', '2025-01-16', 'Permohonan Bantuan Dana', 'Kepala');

-- 3. Create fiter by date (descending)
CREATE INDEX IF NOT EXISTS idx_surat_masuk_tanggal_diterima_desc
ON surat_masuk(tanggal_diterima DESC);

CREATE INDEX IF NOT EXISTS idx_surat_keluar_tanggal_diterima_desc
ON surat_keluar(tanggal_diterima DESC);
-- ...

-- 4. Jika ingin menambahkan pegawai

INSERT INTO pegawai (nip, nama, role, email) VALUES
('199604132025051002', 'Rahman Singet', 'user', 'singet@gmail.com');
