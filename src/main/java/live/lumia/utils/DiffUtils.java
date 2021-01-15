package live.lumia.utils;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DiffUtils {

    @Data
    @Accessors(chain = true)
    public static class DiffResult<O, T> {
        /**
         * 新增对象列表
         */
        private List<T> addedList;
        /**
         * 修改后的对象列表
         */
        private Map<O, T> changedMap;
        /**
         * 已删除对象列表
         */
        private List<O> deletedList;
        /**
         * 相同的
         */
        private Map<O, T> sameMap;
    }

    /**
     * 对比两个List的元素
     * <p>
     * 如果 originList 的元素在 targetList 中存在 PrimaryKey 相等的元素并且 elementComparator 比较结果不相等，则将修改后的值添加到changedList列表中；
     * 如果 originList 的元素在 targetList 中不存在，将baseList中的元素添加到deletedList中；
     * 如果 targetList 的元素在 originList 中不存在，将targetList中的元素添加到addedList中；
     * <p>
     * complexity: O(n)
     *
     * @param originList       基础List(原来的List)
     * @param targetList       目标List(最新的List)
     * @param predicate        元素比较器
     * @param originKeyExtract 主键选择器
     * @param targetKeyExtract 主键选择器
     * @param <T>              T
     * @param <O>              T
     * @param <K>              T
     * @return 对比结果
     */
    public static <O, T, K> DiffResult<O, T> diffList(List<O> originList, List<T> targetList,
                                                      Function<O, K> originKeyExtract,
                                                      Function<T, K> targetKeyExtract,
                                                      BiPredicate<O, T> predicate) {
        DiffResult<O, T> checkResult = checkEmptyAndReturn(originList, targetList);
        if (checkResult != null) {
            return checkResult;
        }

        Map<K, O> originMap = originList.stream().collect(Collectors.toMap(originKeyExtract, x -> x, (x, y) -> x));

        List<T> addedList = new ArrayList<>();
        Map<O, T> changedMap = new HashMap<>();
        Map<O, T> sameMap = new HashMap<>();
        List<O> deletedList = new ArrayList<>();

        //找出新增的 和需要更新的
        for (T target : targetList) {
            K key = targetKeyExtract.apply(target);
            O origin = originMap.get(key);
            if (origin == null) {
                addedList.add(target);
            } else {
                originMap.remove(key);
                if (predicate.test(origin, target)) {
                    sameMap.put(origin, target);
                } else {
                    changedMap.put(origin, target);
                }
            }
        }
        //剩余的就是需要删除的
        Set<Map.Entry<K, O>> entrySet = originMap.entrySet();
        if (CollectionUtils.isNotEmpty(entrySet)) {
            for (Map.Entry<K, O> entry : entrySet) {
                deletedList.add(entry.getValue());
            }
        }
        return new DiffResult<O, T>()
                .setAddedList(addedList)
                .setChangedMap(changedMap)
                .setSameMap(sameMap)
                .setDeletedList(deletedList);
    }


    private static <O, T> DiffResult<O, T> checkEmptyAndReturn(List<O> originList, List<T> targetList) {

        if (CollectionUtils.isEmpty(originList) && CollectionUtils.isEmpty(targetList)) {
            return new DiffResult<>();
        }

        if (CollectionUtils.isEmpty(originList) && CollectionUtils.isNotEmpty(targetList)) {
            return new DiffResult<O, T>().setAddedList(targetList);
        }

        if (CollectionUtils.isNotEmpty(originList) && CollectionUtils.isEmpty(targetList)) {
            return new DiffResult<O, T>().setDeletedList(originList);
        }
        return null;
    }


}