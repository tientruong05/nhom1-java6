package poly.edu.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import poly.edu.dao.SubCategoryDAO;
import poly.edu.entity.SubCategoryEntity;
import poly.edu.repository.SubCategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubCategoryServiceImpl implements SubCategoryService {

    @Autowired
    private SubCategoryDAO subCategoryDAO;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Override
    public List<SubCategoryEntity> getAllSubCategories() {
        return subCategoryDAO.findAll().stream()
                .filter(subCategory -> subCategory != null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public SubCategoryEntity getSubCategoryById(int id) {
        return subCategoryDAO.findById(id);
    }

    @Override
    public void addSubCategory(SubCategoryEntity subCategory) {
        Optional.ofNullable(subCategory).ifPresent(subCategoryDAO::save);
    }

    @Override
    public void updateSubCategory(SubCategoryEntity subCategory) {
        Optional.ofNullable(subCategory).ifPresent(subCategoryDAO::update);
    }

    @Override
    public void deleteSubCategory(int id) {
        Optional.of(id).filter(i -> i > 0).ifPresent(subCategoryDAO::delete);
    }

    @Override
    public Page<SubCategoryEntity> getSubCategoriesByCategory(String categoryName, int page, int size) {
        return subCategoryRepository.findByCategoryName(categoryName, PageRequest.of(page, size));
    }
}